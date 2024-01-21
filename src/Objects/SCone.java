package Objects;
public class SCone extends SObject{
	private float radius;
	private float height;
	private int slices;

	public SCone(){
		super();
		init();
		update();
	}

	public SCone(float radius, float height){
		super();
		init();
		this.radius = radius;
		this.height = height;
		update();
	}

	public SCone(float radius, float height, int slices){
		super();
		this.radius = radius;
		this.height = height;
		this.slices = slices;
		update();
	}
	
	private void init(){
		this.radius = 4;
		this.height = 8;
		this.slices = 40;
	}

	@Override
	protected void genData() {
		double deltaLong = PI * 2 / slices; // Circumference / number of slices

		// Generate vertices coordinates, normal values, and texture coordinates
		numVertices = slices * 4; // vertices = edges + 2 - faces
		vertices = new float[numVertices * 3];
		normals = new float[numVertices * 3];
		textures = new float[numVertices * 2];

		for (int i = 0; i < slices; i++){

			// ---------- Vertices, Normals & Textures of the main face ---------- //
			int cone_base = i * 3; // The index for the base of the cone
			int cone_point = (i  + slices) * 3; // The index for the point of the cone

			normals[cone_base] = cos(deltaLong * i);
			normals[cone_base + 1] = 0;
			normals[cone_base + 2] = sin(deltaLong * i);

			normals[cone_point] = cos(deltaLong * i);
			normals[cone_point + 1] = 0;
			normals[cone_point + 2] = sin(deltaLong * i);

			vertices[cone_base] = radius * normals[cone_base];
			vertices[cone_base + 1] = 0;
			vertices[cone_base + 2] = radius * normals[cone_base + 2];

			vertices[cone_point] = 0;
			vertices[cone_point + 1] = height;
			vertices[cone_point + 2] = 0;

			textures[cone_base] = (float)i / slices;
			textures[cone_base + 1] = 1;

			textures[cone_point] = (float)i / slices;
			textures[cone_point + 1] = 0;

			// ---------- Vertices, Normals & Textures of the base of the object ---------- //

			cone_base = (i + numVertices / 2) * 3; // Index of bottom vertex

			vertices[cone_base] = radius * cos(deltaLong * i);;
			vertices[cone_base + 1] = 0;
			vertices[cone_base + 2] = radius * sin(deltaLong * i);;

			normals[cone_base] = 0;
			normals[cone_base + 1] = -1;
			normals[cone_base + 2] = 0;

			// Index of the texture coordinate for the top vertex of the base
			cone_base = i * 2 + numVertices;

			textures[cone_base] = (float)i/slices;
			textures[cone_base + 1] = 0;

		}

		// Number of vertices * XYZ values
		// (slices * 4) * 3 = 12
		numIndices = slices * 12;
		indices = new int[numIndices];

		//Main face of the cone
		// The index has to be set to i*6
		// as that is the size of the main body
		for (int i = 0; i < slices; i++) {
			indices[i*6] = i + slices;
			indices[i*6+1] = (i + 1) % slices;
			indices[i*6+2] = i;
		}

		// Offset to bypass the main body
		int offset = slices * 6;

		// Base of the cone,
		// Instead of starting at index 0
		// It starts at index 2 so the main
		// body is whole
		for (int i = 2; i < slices; i++) {
			int cone_base = (i - 2) * 3;
			indices[offset + cone_base] = numVertices / 2;
			indices[offset + cone_base + 1] = numVertices / 2 + i - 1;
			indices[offset + cone_base + 2] = numVertices / 2 + i;
		}
	}
	
	public void setRadius(float radius){
		this.radius = radius;
		updated = false;
	}

	public void setHeight(float height){
		this.height = height;
		updated = false;
	}
		
	public void setSlices(int slices){
		this.slices = slices;
		updated = false;
	}
		
	public float getRadius(){
		return radius;
	}

	public float getHeight(){
		return height;
	}
		
	public int getSlices(){
		return slices;
	}
}