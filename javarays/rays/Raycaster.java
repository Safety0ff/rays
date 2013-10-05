package rays;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.Vector;

public final class Raycaster {

    final static class vector {
		public final float x,y,z;  // Vector has three float attributes.
		public vector(){ x=y=z=0.f;}                                  //Empty constructor
		public vector(final vector v){x=v.x;y=v.y;z=v.z;}                                  //Empty constructor
		public vector(final float a,final float b,final float c){x=a;y=b;z=c;}            //Constructor
		public vector add(final vector r) {return new vector(x+r.x,y+r.y,z+r.z);} //Vector add
		public vector pow(final vector r) {return new vector(y*r.z-z*r.y,z*r.x-x*r.z,x*r.y-y*r.x);} //Cross-product
		public float  dot(final vector r)  {return x*r.x+y*r.y+z*r.z;}    //Vector dot product
		public vector mul(final float r)  {return new vector(x*r,y*r,z*r);}       //Vector scaling
		public vector norm(){return mul((float)(1.f/Math.sqrt(dot(this))));} // Used later for normalizing the vector
	};

	final static class object {
		public float k,j;
		object(final float x, final float y){k=x;j=y;}
	};

	final static char[][] art = {
		"                   ".toCharArray(),
		"    1111           ".toCharArray(),
		"   1    1          ".toCharArray(),
		"  1           11   ".toCharArray(),
		"  1          1  1  ".toCharArray(),
		"  1     11  1    1 ".toCharArray(),
		"  1      1  1    1 ".toCharArray(),
		"   1     1   1  1  ".toCharArray(),
		"    11111     11   ".toCharArray()
	};

	static Vector<object> objects = new Vector<>(art.length * art[0].length);

	static void F() {
		final int nr = art.length;
		final int nc = art[0].length;
		for (int k = nc - 1; k >= 0; k--) {
			for (int j = nr - 1; j >= 0; j--) {
				if (art[j][nc - 1 - k] != ' ') {
					objects.add(new object(-k, -(nr - 1 - j)));
				}
			}
		}
	}

    private static final vector STD_VEC = new vector(0, 0, 1);

	static int w = 512, h = 512;
	static byte[] bytes;

	// The '!' are for normalizing each vectors with ! operator.
	static final vector g = (new vector(-5.5f, -16, 0)).norm(); // WTF ? See https://news.ycombinator.com/item?id=6425965 for more.

	static final vector a = (STD_VEC.pow(g)).norm().mul(.002f);
	static final vector b = (g.pow(a)).norm().mul(.002f);
	static final vector c = (a.add(b)).mul(-256).add(g);

	public static void main(final String[] args) throws Exception {
		F();

		int num_threads = Runtime.getRuntime().availableProcessors();

		if(args.length > 0) {
			w = Integer.parseInt(args[0]);
		}

		if(args.length > 1) {
			h = Integer.parseInt(args[1]);
		}

		if(args.length > 2) {
			num_threads = Integer.parseInt(args[2]);
		}

		final BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream("image.ppm"));
		stream.write("".format("P6 %d %d 255 ", w, h).getBytes());

		bytes = new byte[3*w*h];

		final Vector<Thread> threads = new Vector<>();
        for (int i = 0; i < num_threads; ++i) {
            final Thread thread = new Thread(new Worker(i, num_threads));
            thread.start();
            threads.add(thread);
        }

        for(final Thread t : threads) {
            t.join();
        }

        stream.write(bytes);
        stream.flush();
        stream.close();
	}
}
