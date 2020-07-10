package irt.gui.controllers.flash.service;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class PagesCount implements Supplier<PagesCount>, ObjIntConsumer<PagesCount>, BiConsumer<PagesCount, PagesCount> {

	public static final int KB = 1024;
	public static final int[] ALL_PAGES = new int[] { 	16 * KB, 16 * KB, 16 * KB, 16 * KB, 64 * KB, 128 * KB, 128 * KB, 128 * KB, 128 * KB, 128 * KB , 128 * KB, 128 * KB,
														16 * KB, 16 * KB, 16 * KB, 16 * KB, 64 * KB, 128 * KB, 128 * KB, 128 * KB, 128 * KB, 128 * KB , 128 * KB, 128 * KB };
	public static final int MAX_FILE_SIZE = Arrays
											.stream(ALL_PAGES)
											.sum();

	private long fileSize;
	private long pagesSize;

	private int pagesCount = -1; 	public int getPagesCount() { return pagesCount; }
	private byte[] pages; 			public byte[] getPages() { return pages; }

	public PagesCount(long fileSize) {

		this.fileSize = Optional
							.of(fileSize)
							.filter(fs->fs<=MAX_FILE_SIZE)
							.filter(fs->fs>0)
							.get();

		Arrays
		.stream(ALL_PAGES)
		.collect(this, this, this);

		final int[] array = IntStream
				.range(-1, pagesCount + 1)
				.toArray();
		array[0] = pagesCount;

		pages = new byte[array.length*2];
		for(int i=0, x=0; i<array.length; i++){
			pages[x++] = (byte) (array[i]>>8);
			pages[x++] = (byte) array[i];
		}
	}

	@Override
	public PagesCount get() {
		return this;
	}

	@Override
	public void accept(PagesCount t, int value) {

		if(fileSize==0)
			return;

		if(fileSize>pagesSize){
			pagesCount++;
			pagesSize += value;
		}
	}

	@Override
	public void accept(PagesCount t, PagesCount u) {
	}

	@Override
	public String toString() {
		return "PagesCount [fileSize=" + fileSize + ", pagesSize=" + pagesSize + ", pagesCount=" + pagesCount + ", pages=" + Arrays.toString(pages) + "]";
	}
}
