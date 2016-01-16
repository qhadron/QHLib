package qh.q3d;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;

import qh.q3d.camera.Camera;
import qh.q3d.objects.GenericObject3D;
import qh.q3d.objects.Object3D;

public class Parser3D {

	// mandatory data
	private static final String[] VERTICIES_COUNT_FLAG = { "\"verticesCount\":" };
	private static final String[] FACE_COUNT_FLAGS = { "\"indexCount\":" };
	private static final String[] VERTICES_FLAG = { "\"vertices\":[" };
	private static final String[] FACES_FLAG = { "\"indices\":[" };
	private static final String[] NAME_FLAG = { "\"name\":\"" };
	private static final String[] MESH_START_FLAG = { "\"meshes\":[" };
	// optional data
	private static final String[] TEXTURE_STRIDE_FLAG = { "\"uvCount\":" };
	private static final String[] POSITION_FLAG = { "\"position\":[" };
	private static final String[] ROTATION_FLAG = { "\"rotation\":[" };
	private static final String[] SCALE_FLAG = { "\"scaling\":[" };

	// camera
	private static final String[] CAMERA_START_FLAG = { "\"cameras\":[{" };
	private static final String[] TARGET_FLAG = { "\"target\":[" };
	private static final String[] FOV_FLAG = { "\"fov\":" };
	private static final String[] ZNEAR_FLAG = { "\"minZ\":" };
	private static final String[] ZFAR_FLAG = { "\"maxZ\":" };

	public static String getStringFromStream(InputStream s) throws IOException {
		// 10KB buffer
		char[] buffer = new char[10240];
		StringBuffer sb = new StringBuffer((int) s.available());
		InputStreamReader fr = new InputStreamReader(s);
		while (fr.read(buffer) > 0) {
			sb.append(buffer);
		}
		sb.append(buffer);
		fr.close();
		String res = sb.toString();
		sb.setLength(0);
		System.gc();
		return res;
	}

	public static LinkedList<Object3D> getMesh(String filename)
			throws Exception {
		return getMesh(new File(filename).getAbsoluteFile());
	}

	public static LinkedList<Object3D> getMesh(File file) throws IOException {
		return getMeshFromString(getStringFromStream(new FileInputStream(file)));
	}

	public static LinkedList<Object3D> getMesh(InputStream s)
			throws IOException {
		return getMeshFromString(getStringFromStream(s));
	}

	public static LinkedList<Object3D> getMeshFromString(String str)
			throws NumberFormatException {
		LinkedList<Object3D> result = new LinkedList<Object3D>();
		int beginning = locateStart(str, MESH_START_FLAG, -1);

		while (locateStart(str, NAME_FLAG, beginning) > 0) {
			// collecting information to construct the object
			int startpos = -1, endpos = -1;
			startpos = locateStart(str, NAME_FLAG, beginning);
			endpos = locateEnd(str, "\"", startpos);
			if (startpos == -1)
				throw new NumberFormatException("Cannot find start of name");
			if (endpos == -1)
				throw new NumberFormatException("Cannot find end of name");
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
			if (startpos == -1)
				throw new NumberFormatException("Cannot find start of faces");
			if (endpos == -1)
				throw new NumberFormatException("Cannot find end of faces");
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
			if (startpos == -1)
				throw new NumberFormatException(
						"Cannot find start of verticies");
			if (endpos == -1)
				throw new NumberFormatException("Cannot find end of faces");
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
				if (startpos == -1)
					throw new NumberFormatException(
							"Cannot find start of position");
				if (endpos == -1)
					throw new NumberFormatException(
							"Cannot find end of position");
				String[] strPosition = str.substring(startpos, endpos).split(
						",");
				if (strPosition.length > 3)
					throw new NumberFormatException(
							"More than 3 cooridnates for position");
				obj.setPosition(Double.parseDouble(strPosition[0]),
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
				if (startpos == -1)
					throw new NumberFormatException(
							"Cannot find start of rotation");
				if (endpos == -1)
					throw new NumberFormatException(
							"Cannot find end of rotation");
				String[] strRotation = str.substring(startpos, endpos).split(
						",");
				if (strRotation.length > 3)
					throw new NumberFormatException(
							"More than 3 cooridnates for rotation");
				obj.setRotation(Double.parseDouble(strRotation[0]),
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
				if (startpos == -1)
					throw new NumberFormatException(
							"Cannot find start of scale");
				if (endpos == -1)
					throw new NumberFormatException("Cannot find end of scale");
				String[] strScale = str.substring(startpos, endpos).split(",");
				if (strScale.length > 3)
					throw new NumberFormatException(
							"More than 3 cooridnates for scale");
				obj.setScale(Double.parseDouble(strScale[0]),
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

	public static Camera getCamera(String filename) throws Exception {
		return getCamera(new File(filename).getAbsoluteFile());
	}

	public static Camera getCamera(File file) throws IOException {
		return getCameraFromString(getStringFromStream(new FileInputStream(file)));
	}

	public static Camera getCamera(InputStream s) throws IOException {
		return getCameraFromString(getStringFromStream(s));
	}

	public static Camera getCameraFromString(String str) {
		Camera result = new Camera();
		int startpos, endpos, beginning = locateStart(str, CAMERA_START_FLAG,
				-1);

		// read target vector
		startpos = locateStart(str, TARGET_FLAG, beginning);
		endpos = locateEnd(str, "]", startpos);
		String[] target = str.substring(startpos, endpos).split(",");
		if (target.length != 3)
			throw new NumberFormatException(
					"Length of target vector is not equal to 3");
		result.setTarget(Double.parseDouble(target[0]),
				Double.parseDouble(target[1]), Double.parseDouble(target[2]));

		// read position vector
		startpos = locateStart(str, POSITION_FLAG, beginning);
		endpos = locateEnd(str, "]", startpos);
		String[] position = str.substring(startpos, endpos).split(",");
		if (position.length != 3)
			throw new NumberFormatException(
					"Length of position vector is not equal to 3");
		result.setPosition(Double.parseDouble(position[0]),
				Double.parseDouble(position[1]),
				Double.parseDouble(position[2]));

		// read field of view
		double fov = readDouble(str, FOV_FLAG, beginning);
		result.setFov(fov);

		// read clipping spaces
		double znear = readDouble(str, ZNEAR_FLAG, beginning);
		double zfar = readDouble(str, ZFAR_FLAG, beginning);
		result.setFar(zfar);
		result.setNear(znear);

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

	private static double readDouble(String str, String[] possibleNames,
			int start) throws NumberFormatException {
		assert possibleNames.length > 0;
		int startpos = locateStart(str, possibleNames, start), endpos = -1;
		// search for the end of the int
		for (int pos = startpos; pos < str.length(); ++pos) {
			if (!Character.isDigit(str.charAt(pos)) && str.charAt(pos) != '-'
					&& str.charAt(pos) != '.') {
				endpos = pos;
				break;
			}
		}
		if (endpos < 0)
			return -1;
		return Double.parseDouble(str.substring(startpos, endpos));
	}
}
