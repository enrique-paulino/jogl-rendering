import Basic.ShaderProg;
import Basic.Transform;
import Basic.Vec4;
import Objects.SCube;
import Objects.SObject;
import com.jogamp.nativewindow.WindowClosingProtocol;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.jogamp.opengl.GL3.*;


public class CGCW03 {

	final GLWindow window; //Define a window
	final FPSAnimator animator=new FPSAnimator(60, true);
	final Renderer renderer = new Renderer();

	public CGCW03() {
        GLProfile glp = GLProfile.get(GLProfile.GL3);
        GLCapabilities caps = new GLCapabilities(glp);
        window = GLWindow.create(caps);

		window.addGLEventListener(renderer); //Set the window to listen GLEvents

		animator.add(window);

		window.setTitle("Coursework 3");
		window.setSize(500,500);
		window.setDefaultCloseOperation(WindowClosingProtocol.WindowClosingMode.DISPOSE_ON_CLOSE);
		window.setVisible(true);

		animator.start();
	}

	public static void main(String[] args) {
		new CGCW03();

	}

	class Renderer implements GLEventListener {

		private Transform T = new Transform(); //model_view transform

		//VAOs and VBOs parameters
		private int idPoint=0, numVAOs = 2;
		private int idBuffer=0, numVBOs = 2;
		private int idElement=0, numEBOs = 2;
		private int[] VAOs = new int[numVAOs];
		private int[] VBOs = new int[numVBOs];
		private int[] EBOs = new int[numEBOs];

		//Model parameters
		private int[] numElements = new int[numEBOs];
		private long vertexSize;
		private long normalSize;
		private int vPosition;
		private int vNormal;

		//Transformation parameters
		private int ModelView;
		private int NormalTransform;
		private int Projection;

		//Lighting parameter
		private int AmbientProduct;
		private int DiffuseProduct;
		private int SpecularProduct;
		private int Shininess;

		// Material parameters
		private float[] ambient1;
	    private float[] diffuse1;
	    private float[] specular1;
	    private float  materialShininess1;

		// Texture parameters
		Texture textureImage;
		private int vTexCoord;
		private long textureSize;




		@Override
		public void display(GLAutoDrawable drawable) {
			GL3 gl = drawable.getGL().getGL3(); // Get the GL pipeline object this

			gl.glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);

			//Transformation for the first object (a sphere)
			T.initialize();
			T.scale(1f,1f,1f);
			T.rotateX(-20); T.rotateY(-10); T.rotateZ(15);
			T.translate(0, 0, 0);
			//Locate camera
//			T.lookAt(0, 0, 0, 0, 0, -100, 0, 1, 0);  	//Default

			//Send model_view and normal transformation matrices to shader.
			//Here parameter 'true' for transpose means to convert the row-major
			//matrix to column major one, which is required when vertices'
			//location vectors are pre-multiplied by the model_view matrix.
			//Note that the normal transformation matrix is the inverse-transpose
			//matrix of the vertex transformation matrix
			gl.glUniformMatrix4fv( ModelView, 1, true, T.getTransformv(), 0 );
			gl.glUniformMatrix4fv( NormalTransform, 1, true, T.getInvTransformTv(), 0 );

			//send other uniform variables to shader
			gl.glUniform4fv( AmbientProduct, 1, ambient1,0 );
		    gl.glUniform4fv( DiffuseProduct, 1, diffuse1, 0 );
		    gl.glUniform4fv( SpecularProduct, 1, specular1, 0 );
		    gl.glUniform1f( Shininess, materialShininess1);

			idPoint=0;
			idBuffer=0;
			idElement=0;
			bindObject(gl);
		    gl.glDrawElements(GL_TRIANGLES, numElements[idElement], GL_UNSIGNED_INT, 0);
		}

		@Override
		public void init(GLAutoDrawable drawable) {
			GL3 gl = drawable.getGL().getGL3(); // Get the GL pipeline object this


			System.out.print("GL_Version: " + gl.glGetString(GL_VERSION));

			gl.glEnable(GL_CULL_FACE);

			try { // Reads a new file as a texture and sets it to the current object's active texture, and binds it
				textureImage = TextureIO.newTexture(new File("WelshDragon.jpg"), false);
				gl.glActiveTexture(textureImage.getTextureObject());
				textureImage.bind(gl);
			} catch (IOException ex) {
				Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
			}

			//compile and use the shader program
			ShaderProg shaderproc = new ShaderProg(gl, "Texture.vert", "Texture.frag");
			int program = shaderproc.getProgram();
			gl.glUseProgram(program);

			// Initialize the vertex position and normal attribute in the vertex shader
		    vPosition = gl.glGetAttribLocation( program, "vPosition" );
		    vNormal = gl.glGetAttribLocation( program, "vNormal" );
			vTexCoord = gl.glGetAttribLocation(program, "vTexCoord");

		    // Get connected with the ModelView, NormalTransform, and Projection matrices
		    // in the vertex shader
		    ModelView = gl.glGetUniformLocation(program, "ModelView");
		    NormalTransform = gl.glGetUniformLocation(program, "NormalTransform");
		    Projection = gl.glGetUniformLocation(program, "Projection");

			// Setting the fragment shader texture sampler variable
			gl.glUniform1i(gl.glGetUniformLocation(program, "tex"), 0);

		    // Get connected with uniform variables AmbientProduct, DiffuseProduct,
		    // SpecularProduct, and Shininess in the vertex shader
		    AmbientProduct = gl.glGetUniformLocation(program, "AmbientProduct");
		    DiffuseProduct = gl.glGetUniformLocation(program, "DiffuseProduct");
		    SpecularProduct = gl.glGetUniformLocation(program, "SpecularProduct");
		    Shininess = gl.glGetUniformLocation(program, "Shininess");

		    // Generate VAOs, VBOs, and EBOs
		    gl.glGenVertexArrays(numVAOs,VAOs,0);
			gl.glGenBuffers(numVBOs, VBOs,0);
			gl.glGenBuffers(numEBOs, EBOs,0);

		    // Initialize shader lighting parameters
			float[] lightPosition = {10.0f, 10.0f, -10.0f, 0.0f};
			Vec4 lightAmbient = new Vec4(0.7f, 0.7f, 0.7f, 1.0f);
			Vec4 lightDiffuse = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
			Vec4 lightSpecular = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);

		    gl.glUniform4fv( gl.glGetUniformLocation(program, "LightPosition"), 1, lightPosition, 0 );

			// Creates the Cube Object
		    SObject cube = new SCube(1);
			idPoint=0;
			idBuffer=0;
			idElement=0;
			createObject(gl, cube);

			// Setting Cube material
			Vec4 materialAmbient1 = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
			Vec4 materialDiffuse1 = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
			Vec4 materialSpecular1 = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
			materialShininess1 = 64f;

			// Generates lighting information from material and lighting settings
		    Vec4 ambientProduct = lightAmbient.times(materialAmbient1);
		    ambient1 = ambientProduct.getVector();
		    Vec4 diffuseProduct = lightDiffuse.times(materialDiffuse1);
		    diffuse1 = diffuseProduct.getVector();
		    Vec4 specularProduct = lightSpecular.times(materialSpecular1);
		    specular1 = specularProduct.getVector();

		    // This is necessary. Otherwise, the color on back face may display
//		    gl.glDepthFunc(GL_LESS);
		    gl.glEnable(GL_DEPTH_TEST);
		}

		@Override
		public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {

			GL3 gl = drawable.getGL().getGL3(); // Get the GL pipeline object this

			gl.glViewport(x, y, w, h);

			T.initialize();

			//projection
			if(h<1){h=1;}
			if(w<1){w=1;}
			float a = (float) w/ h;   //aspect
			if (w < h) {
				T.ortho(-1, 1, -1/a, 1/a, -1, 1);
			}
			else{
				T.ortho(-1*a, 1*a, -1, 1, -1, 1);
			}

			// Convert right-hand to left-hand coordinate system
			T.reverseZ();
		    gl.glUniformMatrix4fv( Projection, 1, true, T.getTransformv(), 0 );

		}

		@Override
		public void dispose(GLAutoDrawable drawable) {
			System.exit(0);

		}

		public void createObject(GL3 gl, SObject obj) {
			float [] vertexArray = obj.getVertices();
			float [] normalArray = obj.getNormals();
			float [] textureCoordinateArray = obj.getTextures(); // Gets the coordinates of the textures from the object
			int [] vertexIndexs = obj.getIndices();
			numElements[idElement] = obj.getNumIndices();

			bindObject(gl);

			FloatBuffer vertices = FloatBuffer.wrap(vertexArray);
			FloatBuffer normals = FloatBuffer.wrap(normalArray);
			FloatBuffer textures = FloatBuffer.wrap(textureCoordinateArray); // Wraps the texture coordinates as a float buffer

		    // Create an empty buffer with the size we need
			// and a null pointer for the data values
			vertexSize = vertexArray.length*(Float.SIZE/8);
			normalSize = normalArray.length*(Float.SIZE/8);
			textureSize = textureCoordinateArray.length*(Float.SIZE/8);
			gl.glBufferData(GL_ARRAY_BUFFER, vertexSize +normalSize +textureSize, null, GL_STATIC_DRAW); // pay attention to *Float.SIZE/8

			// Load the real data separately.  We put the colors right after the vertex coordinates,
		    // so, the offset for colors is the size of vertices in bytes
		    gl.glBufferSubData( GL_ARRAY_BUFFER, 0, vertexSize, vertices );
		    gl.glBufferSubData( GL_ARRAY_BUFFER, vertexSize, normalSize, normals );
			gl.glBufferSubData( GL_ARRAY_BUFFER, vertexSize + normalSize, textureSize, textures);

			IntBuffer elements = IntBuffer.wrap(vertexIndexs);

			long indexSize = vertexIndexs.length*(Integer.SIZE/8);
			gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexSize, elements, GL_STATIC_DRAW); // pay attention to *Float.SIZE/8

			gl.glEnableVertexAttribArray(vPosition);
			gl.glVertexAttribPointer(vPosition, 3, GL_FLOAT, false, 0, 0L);

			gl.glEnableVertexAttribArray(vNormal);
		    gl.glVertexAttribPointer(vNormal, 3, GL_FLOAT, false, 0, vertexSize);

			// Sending texture coordinates to vertex shader, instead of defining offset i set it to the vertex + normal
			gl.glEnableVertexAttribArray(vTexCoord);
			gl.glVertexAttribPointer(vTexCoord, 2, GL_FLOAT, false, 0, vertexSize+normalSize);

		}

		public void bindObject(GL3 gl){
			gl.glBindVertexArray(VAOs[idPoint]);
			gl.glBindBuffer(GL_ARRAY_BUFFER, VBOs[idBuffer]);
			gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBOs[idElement]);
		};
	}
}