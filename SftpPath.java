package sftp;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class SftpPath implements Comparable<SftpPath> {
	public static void main(String[] args) {
		System.out.println(Path.of("/").getParent());
		System.out.println(Path.of("C:\\").getParent());
	}

	public static SftpPath of(String path) {
		return new SftpPath(path);
	}

	private final String path;

	public SftpPath(String path) {
		if (path.equals("/")) {
			this.path = path;
		} else {
			this.path = path.substring(0, path.length() - (path.endsWith("/") ? 1 : 0));
		}
	}

	public SftpPath resolve(SftpPath other) {
		if (other.isAbsolute()) {
			return other;
		}

		if (isEmpty()) {
			return other;
		}

		if (other.isEmpty()) {
			return this;
		}

		if (isRoot()) {
			return new SftpPath("/" + other.path);
		}

		return new SftpPath(path + "/" + other.path);
	}

	public boolean isEmpty() {
		return path.equals("");
	}

	public boolean isRoot() {
		return path.equals("/");
	}

	public boolean isAbsolute() {
		return path.startsWith("/");
	}

	static String[] split(String str) {
		var l = new ArrayList<String>();
		var f = 0;

		for (var i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '/') {
				l.add(str.substring(f, i));
				f = i + 1;
			}
		}

		l.add(str.substring(f));
		return l.toArray(String[]::new);
	}

	public SftpPath relativize(SftpPath other) {
		if (isAbsolute() != other.isAbsolute()) {
			throw new IllegalArgumentException("different path");
		}

		if (isRoot()) {
			return new SftpPath(other.path.substring(1));
		}

		if (isEmpty()) {
			return new SftpPath(other.path);
		}

		var a = split(path);
		var b = split(other.path);
		var i = 0;

		while (i < a.length && i < b.length && a[i].equals(b[i])) {
			i++;
		}

		var c = new String[a.length - i + b.length - i];
		Arrays.fill(c, 0, a.length - i, "..");
		System.arraycopy(b, i, c, a.length - i, b.length - i);

		return new SftpPath(String.join("/", c));
	}

	public SftpPath getParent() {
		if (isRoot()) {
			return null;
		}

		var i = path.lastIndexOf('/');

		if (i == -1) {
			return null;
		}

		if (i == 0) {
			return SftpPath.of("/");
		}

		return SftpPath.of(path.substring(0, i));
	}
	
	public Path toPath() {
		return Path.of(path);
	}

	@Override
	public int hashCode() {
		return Objects.hash(path);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof SftpPath)) {
			return false;
		}

		SftpPath other = (SftpPath) obj;
		return Objects.equals(path, other.path);
	}

	@Override
	public String toString() {
		return path;
	}

	@Override
	public int compareTo(SftpPath o) {
		return path.compareTo(o.path);
	}
}
