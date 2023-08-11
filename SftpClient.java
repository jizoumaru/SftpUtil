package sftp;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import iter.Iter;

public class SftpClient implements AutoCloseable {
	public static void main(String[] args) {
		try (var sftp = new SftpClient()) {
			var base = SftpPath.of("sftpuser/data");
			sftp.list(base)
				.flatMap(x -> sftp.list(x))
				.flatMap(x -> sftp.list(x))
				.map(x -> base.relativize(x))
				.take(3)
				.forEach(System.out::println);
		}
	}

	private Session session;
	private ChannelSftp sftp;

	private void ensureOpen() {
		try {
			if (session == null) {
				JSch.setConfig("StrictHostKeyChecking", "no");
				var jsch = new JSch();
				session = jsch.getSession("sftpuser", "172.29.129.63", 22);
				session.setPassword("a");
				session.connect();
			}

			if (sftp == null) {
				sftp = (ChannelSftp) session.openChannel("sftp");
				sftp.connect();
			}
		} catch (JSchException e) {
			throw new RuntimeException(e);
		}
	}

	public Iter<SftpPath> list(SftpPath path) {
		Iterable<LsEntry> iterable = () -> {
			ensureOpen();

			try {
				@SuppressWarnings("unchecked")
				Vector<LsEntry> vec = sftp.ls(path.toString());
				return vec.iterator();
			} catch (SftpException e) {
				throw new RuntimeException(e);
			}
		};

		return Iter.from(iterable)
				.map(x -> x.getFilename())
				.filter(x -> !x.equals("."))
				.filter(x -> !x.equals(".."))
				.map(x -> path.resolve(SftpPath.of(x)));
	}

	@Override
	public void close() {
		try (var _sesson = Close.of(session, x -> x.disconnect());
				var _sftp = Close.of(sftp, x -> x.disconnect())) {
		}
	}
}
