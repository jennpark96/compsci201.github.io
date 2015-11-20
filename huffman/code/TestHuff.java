import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import org.junit.Test;

public class TestHuff {

	private static final String[] files = new String[] { "melville.txt", "kjv10.txt", "lena.tif"};
	private static final int[][] divisions = {{768, 457569, 457586}, {1021, 19910884, 19910908}, {2858, 6123750, 6123767}};

	private Processor getProcessor() {
		try {
			return (Processor) Class.forName("HuffProcessor").newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void compare(BitInputStream inA, BitInputStream inB, int start, int stop) {
		int bitA = 0;
		int bitB = 0;
		int index = 0;
		while ((bitA = inA.readBits(1)) != -1 & (bitB = inB.readBits(1)) != -1 && index++ < stop - 1) {
			if (index >= start) {
				assertTrue(bitA == bitB);
			}
		}
		assertTrue(bitA == bitB);
		inA.close();
		inB.close();
	}

	private void testCompress(String fileName, int start, int stop) {
		ByteArrayOutputStream temp = new ByteArrayOutputStream();
		BitInputStream input = new BitInputStream(new File("data/" + fileName));
		BitOutputStream output = new BitOutputStream(temp);
		getProcessor().compress(input, output);
		input.close();
		output.close();

		BitInputStream inA = new BitInputStream(new File("data/" + fileName + ".compressed"));
		BitInputStream inB = new BitInputStream(new ByteArrayInputStream(temp.toByteArray()));

		compare(inA, inB, start, stop);
	}

	private void testDecompress(String fileName) {
		ByteArrayOutputStream temp = new ByteArrayOutputStream();
		BitInputStream input = new BitInputStream(new File("data/" + fileName + ".compressed"));
		BitOutputStream output = new BitOutputStream(temp);
		getProcessor().decompress(input, output);
		input.close();
		output.close();

		BitInputStream inA = new BitInputStream(new File("data/" + fileName));
		BitInputStream inB = new BitInputStream(new ByteArrayInputStream(temp.toByteArray()));

		compare(inA, inB, 0, Integer.MAX_VALUE);
	}
	
	@Test
	public void testCompressHeader() {
		for (int i = 0; i < files.length; i++) {
			testCompress(files[i], 0, divisions[i][0]);
		}
	}
	
	@Test
	public void testCompressBody() {
		for (int i = 0; i < files.length; i++) {
			testCompress(files[i], divisions[i][0], divisions[i][1]);
		}
	}
	
	@Test
	public void testCompressEOF() {
		for (int i = 0; i < files.length; i++) {
			testCompress(files[i], divisions[i][1], divisions[i][2]);
		}
	}

	@Test
	public void testDecompress() {
		for (int i = 0; i < files.length; i++) {
			testDecompress(files[i]);
		}
	}
}