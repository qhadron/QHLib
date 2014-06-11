package qh.q3d;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import qh.q3d.objects.GenericObject3D;

public class Parser3D {

	public static LinkedList<Object3D> parse(String filename) throws Exception {
		return parse(new File(filename).getAbsoluteFile());
	}

	public static LinkedList<Object3D> parse(File file) throws IOException {
		// 10KB buffer
		char[] buffer = new char[10240];
		StringBuffer sb = new StringBuffer((int) file.length());
		FileReader fr = new FileReader(file);
		try {
			while (fr.read(buffer) > 0) {
				sb.append(buffer);
			}
			sb.append(buffer);
			fr.close();
		} catch (IOException e) {
			System.out.println("Unexpected error while reading from file: "
					+ e.getMessage());
			e.printStackTrace();
		}
		return parseString(sb.toString());
	}

	// mandatory data
	private static final String[] VERTICIES_COUNT_FLAG = { "\"verticesCount\":" };
	private static final String[] FACE_COUNT_FLAGS = { "\"indexCount\":" };
	private static final String[] VERTICES_FLAG = { "\"vertices\":[" };
	private static final String[] FACES_FLAG = { "\"indices\":[" };
	private static final String[] NAME_FLAG = { "\"name\":\"" };
	private static final String[] START_FLAG = { "\"meshes\":[" };

	// optional data
	private static final String[] TEXTURE_STRIDE_FLAG = { "\"uvCount\":" };
	private static final String[] POSITION_FLAG = { "\"position\":[" };
	private static final String[] ROTATION_FLAG = { "\"rotation\":[" };
	private static final String[] SCALE_FLAG = { "\"scaling\":[" };

	public static LinkedList<Object3D> parseString(String str)
			throws NumberFormatException {
		LinkedList<Object3D> result = new LinkedList<Object3D>();
		int beginning = locateStart(str, START_FLAG, -1);

		while (locateStart(str, NAME_FLAG, beginning) > 0) {
			//collecting information to construct the object
			int startpos = -1, endpos = -1;
			startpos = locateStart(str, NAME_FLAG, beginning);
			endpos = locateEnd(str, "\"", startpos);
			if (startpos == -1) throw new NumberFormatException("Cannot find start of name");
			if (endpos == -1) throw new NumberFormatException("Cannot find end of name");
			beginning = startpos;
			
			int numFaces = readInt(str, FACE_COUNT_FLAGS, beginning);
			int numVertices = readInt(str, VERTICIES_COUNT_FLAG, beginning);
			int textureSize = 1;
			
			Object3D obj = new GenericObject3D(str.substring(startpos, endpos),
					numVertices, numFaces / 3);
			try {
				switch (readInt(str, TEXTURE_STRIDE_FLAG, beginning)) {
				case 0:
					textureSize = 6;
					break;
				case 1:
					textureSize = 8;
					break;
				case 2:
					textureSize = 10;
					break;
				}
			} catch (NumberFormatException e) {
				textureSize = 1;
			}
			// beginning of usefull data
			// search for an optional name
			

			// read Faces
			startpos = locateStart(str, FACES_FLAG, beginning);
			endpos = locateEnd(str, "]", startpos);
			if (startpos == -1) throw new NumberFormatException("Cannot find start of faces");
			if (endpos == -1) throw new NumberFormatException("Cannot find end of faces");
			String[] strFaces = str.substring(startpos, endpos).split(",");
			for (int i = 0, cnt = 0; cnt < strFaces.length; ++i) {
				obj.triangles[i] = new Triangle(
						Integer.parseInt(strFaces[cnt++]),
						Integer.parseInt(strFaces[cnt++]),
						Integer.parseInt(strFaces[cnt++]));
			}

			// read vertices
			startpos = locateStart(str, VERTICES_FLAG, beginning);
			endpos = locateEnd(str, "]", startpos);
			if (startpos == -1) throw new NumberFormatException("Cannot find start of verticies");
			if (endpos == -1) throw new NumberFormatException("Cannot find end of faces");
			String[] strVertices = str.substring(startpos, endpos).split(",");

			// pre-subtract the size of each vertex from the texture size to
			// skip texture coordinates
			if (textureSize != 1)
				textureSize -= 3;
			for (int i = 0, cnt = 0; cnt < strVertices.length; ++i) {
				obj.vertices[i] = new Vertex(
						Double.parseDouble(strVertices[cnt++]),
						Double.parseDouble(strVertices[cnt++]),
						Double.parseDouble(strVertices[cnt++]));
				// skipping texture coords for now
				cnt += textureSize;
			}
			// adding the 3 back to texture coordinates
			if (textureSize != 1)
				textureSize += 3;

			// read position
			try {
				startpos = locateStart(str, POSITION_FLAG, beginning);
				endpos = locateEnd(str, "]", startpos);
				if (startpos == -1) throw new NumberFormatException("Cannot find start of position");
				if (endpos == -1) throw new NumberFormatException("Cannot find end of position");
				String[] strPosition = str.substring(startpos, endpos).split(
						",");
				if (strPosition.length > 3)
					throw new NumberFormatException(
							"More than 3 cooridnates for position");
				obj.position.set(Double.parseDouble(strPosition[0]),
						Double.parseDouble(strPosition[1]),
						Double.parseDouble(strPosition[2]));
			} catch (NumberFormatException e) {
				System.out
						.println("Setting position failed: " + e.getMessage());
			}

			// read rotation
			try {
				startpos = locateStart(str, ROTATION_FLAG, beginning);
				endpos = locateEnd(str, "]", startpos);
				if (startpos == -1) throw new NumberFormatException("Cannot find start of rotation");
				if (endpos == -1) throw new NumberFormatException("Cannot find end of rotation");
				String[] strRotation = str.substring(startpos, endpos).split(
						",");
				if (strRotation.length > 3)
					throw new NumberFormatException(
							"More than 3 cooridnates for rotation");
				obj.rotation.set(Double.parseDouble(strRotation[0]),
						Double.parseDouble(strRotation[1]),
						Double.parseDouble(strRotation[2]));
			} catch (NumberFormatException e) {
				System.out
						.println("Setting rotation failed: " + e.getMessage());
			}

			// read scale
			try {
				startpos = locateStart(str, SCALE_FLAG, beginning);
				endpos = locateEnd(str, "]", startpos);
				if (startpos == -1) throw new NumberFormatException("Cannot find start of scale");
				if (endpos == -1) throw new NumberFormatException("Cannot find end of scale");
				String[] strScale = str.substring(startpos, endpos).split(",");
				if (strScale.length > 3)
					throw new NumberFormatException(
							"More than 3 cooridnates for scale");
				obj.scale.set(Double.parseDouble(strScale[0]),
						Double.parseDouble(strScale[1]),
						Double.parseDouble(strScale[2]));
			} catch (NumberFormatException e) {
				System.out.println("Setting scale failed: " + e.getMessage());
			}
			result.add(obj);
			beginning = startpos;
		}
		return result;
	}

	private static int locateStart(String str, String[] possibleNames,
			int lastStart) {
		assert possibleNames.length > 0;
		int startpos = -1; // vertices count pos
		for (int i = 0; i < possibleNames.length; ++i) {
			startpos = str.indexOf(possibleNames[i], lastStart);
			if (startpos != -1) {
				startpos += possibleNames[i].length();
				break;
			}
		}
		if (startpos < 0) {
			return -1;
		}
			
		return startpos;
	}

	private static int locateEnd(String str, String seq, int start) {
		assert seq.length() > 0;
		int endpos = str.indexOf(seq, start);
		if (endpos < 0)
			return -1;
		return endpos;
	}

	private static int readInt(String str, String[] possibleNames, int start)
			throws NumberFormatException {
		assert possibleNames.length > 0;
		int startpos = locateStart(str, possibleNames, start), endpos = -1;
		// search for the end of the int
		for (int pos = startpos; pos < str.length(); ++pos) {
			if (!Character.isDigit(str.charAt(pos)) && str.charAt(pos) != '-') {
				endpos = pos;
				break;
			}
		}
		if (endpos < 0)
			return -1;
		return Integer.parseInt(str.substring(startpos, endpos));
	}

	public static void main(String[] args) {
		try {
			LinkedList<Object3D> susanne = Parser3D
					.parse("C:\\Users\\jackl_000\\Desktop\\cube.babylon");
			for (Iterator<Object3D> it = susanne.iterator(); it.hasNext(); ) {
				Object3D cur = it.next();
				System.out.println("Loaded: " + cur + "\n");
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

}
