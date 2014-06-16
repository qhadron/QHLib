/**
 * 
 */
package qh.q3d.objects;


/**
 * @author jackl_000
 *
 */
public class GenericObject3D extends Object3D {

	
	public GenericObject3D(int verticiesCount, int facesCount) {
		super(verticiesCount, facesCount);
	}
	
	public GenericObject3D(String name, int verticiesCount, int facesCount) {
		super(name,verticiesCount,facesCount);
	}
	
}
