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


public class CGCW04 {

    final GLWindow window;
    final FPSAnimator animator = new FPSAnimator(60, true);
    final Renderer renderer = new Renderer();

    public CGCW04() { // Creates the window that the JOGL program will run in

        GLProfile glp = GLProfile.get(GLProfile.GL3);
        GLCapabilities caps = new GLCapabilities(glp);
        window = GLWindow.create(caps);

        window.addGLEventListener(renderer);

        animator.add(window);

        window.setTitle("Coursework 4");
        window.setSize(500,500);
        window.setDefaultCloseOperation(WindowClosingProtocol.WindowClosingMode.DISPOSE_ON_CLOSE);
        window.setVisible(true);

        animator.start();

    }

    public static void main(String[] args) {
        new CGCW04();
    }

    class Renderer implements GLEventListener {

        // Object's transform
        private Transform T = new Transform();

        // Reference to shader program
        ShaderProg shaderproc;
        int program;

        //VAOs and VBOs parameters
        private int idPoint = 0, numVAOs = 2;
        private int idBuffer = 0, numVBOs = 2;
        private int idElement = 0, numEBOs = 2;
        private int[] VAOs = new int[numVAOs];
        private int[] VBOs = new int[numVBOs];
        private int[] EBOs = new int[numEBOs];

        //Model parameters
        private int[] numElements = new int[numEBOs];
        private long vertexSize;
        private long normalSize;
        private long textureSize;
        private int vPosition;
        private int vNormal;
        private int vTexCoord;


        //Transformation parameters
        private int ModelView;
        private int NormalTransform;
        private int Projection;

        //Lighting parameter
        private int AmbientProduct;
        private int DiffuseProduct;
        private int SpecularProduct;
        private int Shininess;

        // Material parameter → First Object
        private float[] ambient1;
        private float[] diffuse1;
        private float[] specular1;
        private float materialShininess1;

        // Material parameter → First Object
        private float[] ambient2;
        private float[] diffuse2;
        private float[] specular2;
        private float materialShininess2;

        // Texture parameters
        Texture textureImage;
        Texture textureImage2;
        Texture normalMap;
        Texture emptyNormalMap;

        @Override
        public void init(GLAutoDrawable drawable) {
            GL3 gl = drawable.getGL().getGL3();
            gl.glEnable(GL_CULL_FACE);

            System.out.print("GL_Version: " + gl.glGetString(GL_VERSION));

            try {
                // Loads the two textures, one for each object
                textureImage = TextureIO.newTexture(new File("WelshDragon.jpg"), false);
                textureImage2 = TextureIO.newTexture(new File("PortugueseFlag.jpg"), false);

                // Loads one normal map with textures and the other
                // empty to send a normal map to the shader without causing problems (for the second object)
                normalMap = TextureIO.newTexture(new File("brick_normal_map.png"), false);
                emptyNormalMap = TextureIO.newTexture(new File("empty_normal_map.png"), false);

            } catch (IOException ex) { // Catches exceptions where the file wasn't found, or wrong file type etc.
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }

            // Initialise the shader program and passes through the .vert and .frag files
            shaderproc = new ShaderProg(gl, "TextureBumpMap.vert", "TextureBumpMap.frag");
            program = shaderproc.getProgram();
            gl.glUseProgram(program);

            // Sends the position, normals, and texture coordinates to the shader
            vPosition = gl.glGetAttribLocation(program, "vPosition");
            vNormal = gl.glGetAttribLocation(program, "vNormal");
            vTexCoord = gl.glGetAttribLocation(program, "vTexCoord");

            // Sends the camera / view data to the shader
            ModelView = gl.glGetUniformLocation(program, "ModelView");
            NormalTransform = gl.glGetUniformLocation(program, "NormalTransform");
            Projection = gl.glGetUniformLocation(program, "Projection");

            // Sends the product of material and lighting to the shader
            AmbientProduct = gl.glGetUniformLocation(program, "AmbientProduct");
            DiffuseProduct = gl.glGetUniformLocation(program, "DiffuseProduct");
            SpecularProduct = gl.glGetUniformLocation(program, "SpecularProduct");
            Shininess = gl.glGetUniformLocation(program, "Shininess");

            // Generate VAOs, VBOs, and EBOs
            gl.glGenVertexArrays(numVAOs, VAOs, 0);
            gl.glGenBuffers(numVBOs, VBOs, 0);
            gl.glGenBuffers(numEBOs, EBOs, 0);

            // Initialize shader lighting parameters
            float[] lightPosition = {-30.0f, 15.0f, 20.0f, 1.0f};
            Vec4 lightAmbient = new Vec4(0.7f, 0.7f, 0.7f, 1.0f);
            Vec4 lightDiffuse = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
            Vec4 lightSpecular = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);

            // Creates the first cube
            SObject cube1 = new SCube(1);
            idPoint = 0;
            idBuffer = 0;
            idElement = 0;
            createObject(gl, cube1);

            // Set cube material
            Vec4 materialAmbient1 = new Vec4(1f, 1f, 1f, 1f);
            Vec4 materialDiffuse1 = new Vec4(1f, 1f, 1f, 1f);
            Vec4 materialSpecular1 = new Vec4(1f, 1f, 1f, 1f);
            materialShininess1 = 64.0f;

            // Calculates lighting settings from the materials
            Vec4 ambientProduct = lightAmbient.times(materialAmbient1);
            ambient1 = ambientProduct.getVector();
            Vec4 diffuseProduct = lightDiffuse.times(materialDiffuse1);
            diffuse1 = diffuseProduct.getVector();
            Vec4 specularProduct = lightSpecular.times(materialSpecular1);
            specular1 = specularProduct.getVector();

            // Sends lighting/material information to the shaders
            gl.glUniform4fv(AmbientProduct, 1, ambient1, 0);
            gl.glUniform4fv(DiffuseProduct, 1, diffuse1, 0);
            gl.glUniform4fv(SpecularProduct, 1, specular1, 0);
            gl.glUniform4fv(gl.glGetUniformLocation(program, "LightPosition"), 1, lightPosition, 0);
            gl.glUniform1f(Shininess, materialShininess1);

            // Defines the second cube here, and sets its ID value to +1 of the previous
            SObject cube2 = new SCube(1);
            idPoint=1;
            idBuffer=1;
            idElement=1;
            createObject(gl, cube2);

            // Set second cube's materials
            Vec4 materialAmbient2 = new Vec4(1f, 1f, 1f, 1f);
            Vec4 materialDiffuse2 = new Vec4(1f, 1f, 1f, 1f);
            Vec4 materialSpecular2 = new Vec4(1f, 1f, 1f, 1f);
            materialShininess2 = 76.8f;

            // Generates lighting information based on the material and light settings
            ambientProduct = lightAmbient.times(materialAmbient2);
            ambient2 = ambientProduct.getVector();
            diffuseProduct = lightDiffuse.times(materialDiffuse2);
            diffuse2 = diffuseProduct.getVector();
            specularProduct = lightSpecular.times(materialSpecular2);
            specular2 = specularProduct.getVector();

            // This is necessary. Otherwise, the The color on back face may display
//		    gl.glDepthFunc(GL_LESS);

            gl.glEnable(GL_DEPTH_TEST);
        }

        @Override
        public void dispose(GLAutoDrawable glAutoDrawable) {
            System.exit(0);
        }

        @Override
        public void display(GLAutoDrawable drawable) {
            GL3 gl = drawable.getGL().getGL3(); // Get the GL pipeline object this

            gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Transformation for the first cube
            T.initialize();
            T.scale(0.5f, 0.5f, 0.5f);
            T.rotateX(-20);
            T.rotateY(-10);
            T.rotateZ(15);
            T.translate(-0.4f, 0f, 0);

            //Locate camera
//			T.lookAt(0, 0, 0, 0, 0, -100, 0, 1, 0);  	//Default

            //Send model_view and normal transformation matrices to shader.
            //Here parameter 'true' for transpose means to convert the row-major
            //matrix to column major one, which is required when vertices'
            //location vectors are pre-multiplied by the model_view matrix.
            //Note that the normal transformation matrix is the inverse-transpose
            //matrix of the vertex transformation matrix
            gl.glUniformMatrix4fv(ModelView, 1, true, T.getTransformv(), 0);
            gl.glUniformMatrix4fv(NormalTransform, 1, true, T.getInvTransformTv(), 0);

            //send other uniform variables to shader
            gl.glUniform4fv(AmbientProduct, 1, ambient1, 0);
            gl.glUniform4fv(DiffuseProduct, 1, diffuse1, 0);
            gl.glUniform4fv(SpecularProduct, 1, specular1, 0);
            gl.glUniform1f(Shininess, materialShininess1);

            // Sets the active texture to the image texture and binds it
            gl.glActiveTexture(GL_TEXTURE0 + (textureImage.getTextureObject()));
            textureImage.bind(gl);

            // Sets the active texture to the normal map and binds it
            gl.glActiveTexture(GL_TEXTURE0 + normalMap.getTextureObject());
            normalMap.bind(gl);

            // Sends both textures to the shader program
            gl.glUniform1i(gl.glGetUniformLocation(program, "imgTexture"), textureImage.getTextureObject());
            gl.glUniform1i(gl.glGetUniformLocation(program, "normalMap"), normalMap.getTextureObject());

            // Binds all above information to the object with ID=0
            idPoint = 0;
            idBuffer = 0;
            idElement = 0;
            bindObject(gl);
            gl.glDrawElements(GL_TRIANGLES, numElements[idElement], GL_UNSIGNED_INT, 0);

            // Send other uniform variables to shader
            gl.glUniform4fv(AmbientProduct, 1, ambient2, 0);
            gl.glUniform4fv(DiffuseProduct, 1, diffuse2, 0);
            gl.glUniform4fv(SpecularProduct, 1, specular2, 0);
            gl.glUniform1f(Shininess, materialShininess2);

            // Initialize the second object, also a cube, and transforms it so it matches the previous object,
            // but is moved to the side of it.
            T.initialize();
            T.scale(0.5f, 0.5f, 0.5f);
            T.rotateX(-20);
            T.rotateY(-10);
            T.rotateZ(15);
            T.translate(0.4f, 0f, 0);
            gl.glUniformMatrix4fv(ModelView, 1, true, T.getTransformv(), 0);

            // Sets the active texture to the image texture and binds it
            gl.glActiveTexture(GL_TEXTURE0 + textureImage2.getTextureObject());
            textureImage2.bind(gl);

            // Sets the active texture to the normal map and binds it
            gl.glActiveTexture(GL_TEXTURE0 + emptyNormalMap.getTextureObject());
            emptyNormalMap.bind(gl);

            // Sends both textures to the shader program
            gl.glUniform1i(gl.glGetUniformLocation(program, "imgTexture"), textureImage2.getTextureObject());
            gl.glUniform1i(gl.glGetUniformLocation(program, "normalMap"), emptyNormalMap.getTextureObject());

            // Binds all above information to the object with ID=1
            idPoint = 1;
            idBuffer = 1;
            idElement = 1;
            bindObject(gl);
            gl.glDrawElements(GL_TRIANGLES, numElements[idElement], GL_UNSIGNED_INT, 0);
        }

        @Override
        public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
            GL3 gl = drawable.getGL().getGL3(); // Get the GL pipeline object this

            gl.glViewport(x, y, w, h);

            T.initialize();

            //projection
            if (h < 1) {
                h = 1;
            }
            if (w < 1) {
                w = 1;
            }
            float a = (float) w / h;   //aspect
            if (w < h) {
                T.ortho(-1, 1, -1 / a, 1 / a, -1, 1);
            } else {
                T.ortho(-1 * a, 1 * a, -1, 1, -1, 1);
            }

            // Convert right-hand to left-hand coordinate system
            T.reverseZ();
            gl.glUniformMatrix4fv(Projection, 1, true, T.getTransformv(), 0);

        }

        public void createObject(GL3 gl, SObject obj) {

            // Gets values stored in the object and fills an array with it
            // including: vertices, normals, texture coordinates, and indices
            float[] vertexArray = obj.getVertices();
            float[] normalArray = obj.getNormals();
            float[] textureCoordinateArray = obj.getTextures();
            int[] vertexIndexs = obj.getIndices();
            numElements[idElement] = obj.getNumIndices();

            bindObject(gl);

            // Wraps the vertices, normals and textures as a float buffer
            // Wraps the number of vertices as an int buffer
            FloatBuffer vertices = FloatBuffer.wrap(vertexArray);
            FloatBuffer normals = FloatBuffer.wrap(normalArray);
            FloatBuffer textures = FloatBuffer.wrap(textureCoordinateArray);
            IntBuffer elements = IntBuffer.wrap(vertexIndexs);

            // Create an empty buffer with the size we need
            // and a null pointer for the data values
            vertexSize = vertexArray.length * (Float.SIZE / 8);
            normalSize = normalArray.length * (Float.SIZE / 8);
            textureSize = textureCoordinateArray.length * (Float.SIZE / 8);
            gl.glBufferData(GL_ARRAY_BUFFER, vertexSize + normalSize + textureSize,
                    null, GL_STATIC_DRAW);

            // Creates an empty buffer with the size, and a null pointer, for the size of the vertices
            long indexSize = vertexIndexs.length * (Integer.SIZE / 8);
            gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexSize, elements, GL_STATIC_DRAW);

            // Load the real data separately.  We put the colors right after the vertex coordinates,
            // so, the offset for colors is the size of vertices in bytes
            gl.glBufferSubData(GL_ARRAY_BUFFER, 0, vertexSize, vertices);
            gl.glBufferSubData(GL_ARRAY_BUFFER, vertexSize, normalSize, normals);
            gl.glBufferSubData(GL_ARRAY_BUFFER, vertexSize + normalSize, textureSize, textures);

            // Enables the attribute arrays for the vertex positions, normals and texture coordinates
            gl.glEnableVertexAttribArray(vPosition);
            gl.glEnableVertexAttribArray(vNormal);
            gl.glEnableVertexAttribArray(vTexCoord);

            // Sending vertex positions, vertex normals and texture coordinates to vertex shader
            // at an offset of the total of the size of the previous values
            gl.glVertexAttribPointer(vPosition, 3, GL_FLOAT, false, 0, 0L);
            gl.glVertexAttribPointer(vNormal, 3, GL_FLOAT, false, 0, vertexSize);
            gl.glVertexAttribPointer(vTexCoord, 2, GL_FLOAT, false, 0, vertexSize + normalSize);
        }

        public void bindObject(GL3 gl) {
            gl.glBindVertexArray(VAOs[idPoint]);
            gl.glBindBuffer(GL_ARRAY_BUFFER, VBOs[idBuffer]);
            gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBOs[idElement]);
        }


    }

}
