package sftp;
import java.util.function.Consumer;

public class Close<T> implements AutoCloseable {
	public static <T> Close<T> of(T obj, Consumer<T> consumer) {
		return new Close<T>(obj, consumer);
	}

	private final T obj;
	private final Consumer<T> consumer;

	public Close(T obj, Consumer<T> consumer) {
		this.obj = obj;
		this.consumer = consumer;
	}

	@Override
	public void close() {
		if (obj != null) {
			consumer.accept(obj);
		}
	}
}
