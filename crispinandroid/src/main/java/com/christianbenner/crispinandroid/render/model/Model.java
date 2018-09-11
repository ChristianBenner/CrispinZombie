package com.christianbenner.crispinandroid.render.model;

import android.content.Context;

import com.christianbenner.crispinandroid.render.data.Texel;
import com.christianbenner.crispinandroid.render.data.Vertex;
import com.christianbenner.crispinandroid.render.util.VertexArray;
import com.christianbenner.crispinandroid.render.shaders.ShaderConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Christian Benner on 19/01/2018.
 */

public class Model {
    protected final int BYTES_PER_FLOAT = ShaderConstants.BYTES_PER_FLOAT;
    protected final int FLOATS_PER_VERTEX = ShaderConstants.FLOATS_PER_VERTEX;
    protected final int FLOATS_PER_NORMAL = ShaderConstants.FLOATS_PER_NORMAL;
    protected final int FLOATS_PER_TEXEL = ShaderConstants.FLOATS_PER_TEXEL;
    private final int ASCII_FULLSTOP = 46;
    private final int ASCII_ZERO = 48;
    private final int ASCII_NINE = 57;
    private final int ASCII_MINUS = 45;
    private final String ID_VERTEX = "v ";
    private final String ID_NORMAL = "vn";
    private final String ID_TEXEL = "vt";
    private final String ID_FACE = "f ";
    private final int TYPE_UNDEFINED = 0;
    private final int TYPE_VERTEX = 1;
    private final int TYPE_NORMAL = 2;
    private final int TYPE_TEXEL = 3;

    protected int stride;
    protected int vertexCount;
    protected int vertexStartPosition;
    protected int texelStartPosition;
    protected int normalStartPosition;
    protected boolean modelLoaded;
    protected boolean verticesLoaded;
    protected boolean texelsLoaded;
    protected boolean normalsLoaded;

    private Context context;
    protected VertexArray vertexArray;

    private ArrayList<Vertex> vArray;
    private ArrayList<Texel> tArray;
    private ArrayList<Vertex> nArray;
    private ArrayList<ModelPolygon> fArray;
    private float[] floatData;
    private BufferedReader reader;
    private boolean loadVertex = true;
    private boolean loadTexel = false;
    private boolean loadNormal = false;
    private boolean dataTypesSpecified = false;
    private boolean foundVertex = false;
    private boolean foundNormal = false;
    private boolean foundTexel = false;
    private int resourceId;
    private AllowedData allowedData;

    public enum AllowedData
    {
        VERTEX_ONLY,
        VERTEX_TEXEL,
        VERTEX_NORMAL,
        VERTEX_TEXEL_NORMAL
    }

    // Constructor for each different shader type in CrispinEngine
    public Model(Context context, int resourceId)
    {
        // By default look for everything
        init(context, resourceId, AllowedData.VERTEX_TEXEL_NORMAL);
    }

    public Model(Context context, int resourceId, AllowedData modelData)
    {
        this.dataTypesSpecified = true;
        init(context, resourceId, modelData);
    }

    private void init(Context context, int resourceId, AllowedData modelData)
    {
        this.resourceId = resourceId;
        this.context = context;
        this.allowedData = modelData;

        // Determine what data to load
        switch (modelData)
        {
            case VERTEX_TEXEL:
                loadTexel = true;
                break;
            case VERTEX_NORMAL:
                loadNormal = true;
                break;
            case VERTEX_TEXEL_NORMAL:
                loadTexel = true;
                loadNormal = true;
                break;
        }

        loadModel(resourceId);
        if(modelLoaded)
        {
            vertexArray = new VertexArray(floatData);
        }
    }

    protected boolean containsVertices()
    {
        return verticesLoaded;
    }

    protected boolean containsTexels()
    {
        return texelsLoaded;
    }

    protected boolean containsNormals()
    {
        return normalsLoaded;
    }

    // Support texel model loading
    private void loadModel(int resourceId)
    {
        reader = new BufferedReader
                (new InputStreamReader(context.getResources().openRawResource(resourceId)));

        vArray = new ArrayList<Vertex>();
        tArray = new ArrayList<Texel>();
        nArray = new ArrayList<Vertex>();
        fArray = new ArrayList<ModelPolygon>();

        modelLoaded = false;
        verticesLoaded = false;
        texelsLoaded = false;
        normalsLoaded = false;

        vertexCount = 0;
        stride = 0;

        vertexStartPosition = 0;
        texelStartPosition = FLOATS_PER_VERTEX;

        while(processLine()) {}
        generateData();
    }

    private boolean processLine()
    {
        String line = null;
        try
        {
            line = reader.readLine();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        if(line == null)
        {
            return false;
        }
        else
        {
            // Processes Vertex, Texels, Normals
            if(line.charAt(0) == 'v')
            {
                findFloats(line);
            }
            // Processes Faces
            else if(line.charAt(0) == 'f')
            {
                findInts(line);
            }
        }

        return true;
    }

    private void generateData()
    {
        // Calculate how many floats in the data
        int size = 0;
        if(vArray.size() > 0)
        {
            size += fArray.size() * FLOATS_PER_VERTEX;
            verticesLoaded = true;
            stride += FLOATS_PER_VERTEX * BYTES_PER_FLOAT;
        }
        else if(loadVertex && dataTypesSpecified)
        {
            System.err.println("Model::generatedData: Vertex data asked for but not found");
        }

        if(nArray.size() > 0)
        {
            size += fArray.size() * FLOATS_PER_NORMAL;
            normalsLoaded = true;
            stride += FLOATS_PER_NORMAL * BYTES_PER_FLOAT;
        }
        else if(loadNormal && dataTypesSpecified)
        {
            System.err.println("Model::generatedData: Normal data asked for but not found");
        }

        if(tArray.size() > 0)
        {
            size += fArray.size() * FLOATS_PER_TEXEL;
            texelsLoaded = true;
            stride += FLOATS_PER_TEXEL * BYTES_PER_FLOAT;
        }
        else if(loadTexel && dataTypesSpecified)
        {
            System.err.println("Model::generatedData: Texel data asked for but not found");
        }

        floatData = new float[size];
        int floatDataIterator = 0;
        for(int i = 0; i < fArray.size(); i++)
        {
            // Valid to add vertex data
            if(fArray.get(i).vertex != -1)
            {
                Vertex vertex = vArray.get(fArray.get(i).vertex - 1);
                floatData[floatDataIterator++] = vertex.x;
                floatData[floatDataIterator++] = vertex.y;
                floatData[floatDataIterator++] = vertex.z;
                vertexCount++;

                //System.out.println("Vertex: x[" + vertex.x + "], y[" +
                //        vertex.y + "], z[" + vertex.z + "]");
            }

            // Valid to add texel data
            if(fArray.get(i).texel != -1)
            {
                Texel texel = tArray.get(fArray.get(i).texel - 1);
                floatData[floatDataIterator++] = texel.s;
                floatData[floatDataIterator++] = 1.0f - texel.t;

                //System.out.println("Texel: s[" + texel.s + "], t[" + texel.t + "]");
            }

            // Valid to add normal data
            if(fArray.get(i).normal != -1)
            {
                Vertex normal = nArray.get(fArray.get(i).normal - 1);
                floatData[floatDataIterator++] = normal.x;
                floatData[floatDataIterator++] = normal.y;
                floatData[floatDataIterator++] = normal.z;
            }
        }

        if(floatDataIterator != 0)
        {
            modelLoaded = true;
        }

        if(normalsLoaded && !texelsLoaded)
        {
            normalStartPosition = FLOATS_PER_VERTEX;
        }
        else if(normalsLoaded && texelsLoaded)
        {
            normalStartPosition = FLOATS_PER_VERTEX + FLOATS_PER_TEXEL;
        }
    }

    private void findFloats(String line)
    {
        if(line.length() > 2)
        {
            // Float Data
            ArrayList<Float> data = new ArrayList<Float>();

            // Detect the type of data (vertex, normal or texel)
            String id = line.substring(0, 2);
            int idType = TYPE_UNDEFINED;
            boolean processLine = true;
            if(id.equals(ID_VERTEX))
            {
                idType = TYPE_VERTEX;
                foundVertex = true;
                if(!loadVertex)
                {
                    processLine = false;
                }
            }
            else if(id.equals(ID_NORMAL))
            {
                idType = TYPE_NORMAL;
                foundNormal = true;
                if(!loadNormal)
                {
                    processLine = false;
                }
            }
            else if(id.equals(ID_TEXEL))
            {
                idType = TYPE_TEXEL;
                foundTexel = true;
                if(!loadTexel)
                {
                    processLine = false;
                }
            }

            // Push floats in string into floats array
            if(processLine) {
                String floatBuild = "";
                boolean push = false;
                for (int i = 0; i < line.length(); i++) {
                    if ((int) line.charAt(i) == ASCII_FULLSTOP || (int) line.charAt(i) == ASCII_MINUS ||
                            ((int) line.charAt(i) >= ASCII_ZERO && (int) line.charAt(i) <= ASCII_NINE)) {
                        // Numbers and points only
                        floatBuild += line.charAt(i);
                    } else {
                        push = true;
                    }

                    if (push || i == line.length() - 1) {
                        push = false;
                        if (floatBuild.length() != 0) {
                            data.add(Float.parseFloat(floatBuild));
                            floatBuild = "";
                        }
                    }
                }

                // Add to vertex, normal or texel array
                if (data.size() >= 3 && (idType == TYPE_VERTEX || idType == TYPE_NORMAL)) {
                    if (idType == TYPE_VERTEX) {
                        vArray.add(new Vertex(data.get(0), data.get(1), data.get(2)));
                    } else if (idType == TYPE_NORMAL) {
                        nArray.add(new Vertex(data.get(0), data.get(1), data.get(2)));
                    }
                } else if (data.size() >= 2 && idType == TYPE_TEXEL) {
                    tArray.add(new Texel(data.get(0), data.get(1)));
                } else {
                    System.out.println("Error, could not read float line in obj model");
                }
            }
        }
    }

    private void findInts(String line)
    {
        if(line.length() > 2)
        {
            ArrayList<Integer> faces = new ArrayList<Integer>();
            String id = line.substring(0, 2);

            String integerBuild = "";
            boolean push = false;
            for(int i = 0; i < line.length(); i++)
            {
                if((int)line.charAt(i) == ASCII_MINUS ||
                        ((int)line.charAt(i) >= ASCII_ZERO && (int)line.charAt(i) <= ASCII_NINE))
                {
                    // Numbers and points only
                    integerBuild += line.charAt(i);
                }
                else
                {
                    push = true;
                }

                if(push || i == line.length() - 1)
                {
                    push = false;
                    if(integerBuild.length() != 0)
                    {
                        if(id.equals(ID_FACE))
                        {
                            faces.add(Integer.parseInt(integerBuild));
                        }
                        integerBuild = "";
                    }
                }
            }

            // Add the face data
            ModelPolygon[] ModelPolygon = new ModelPolygon[3];
            if(foundVertex && foundTexel && foundNormal)
            {
                // Data Contains Vertices, Texels and Normals
                if(loadVertex && loadTexel && loadNormal)
                {
                    ModelPolygon[0] = new ModelPolygon(faces.get(0), faces.get(1), faces.get(2));
                    ModelPolygon[1] = new ModelPolygon(faces.get(3), faces.get(4), faces.get(5));
                    ModelPolygon[2] = new ModelPolygon(faces.get(6), faces.get(7), faces.get(8));
                }
                else if(loadVertex && loadTexel)
                {
                    ModelPolygon[0] = new ModelPolygon(faces.get(0), faces.get(1), true);
                    ModelPolygon[1] = new ModelPolygon(faces.get(3), faces.get(4), true);
                    ModelPolygon[2] = new ModelPolygon(faces.get(6), faces.get(7), true);
                }
                else if(loadVertex && loadNormal)
                {
                    ModelPolygon[0] = new ModelPolygon(faces.get(0), faces.get(2), false);
                    ModelPolygon[1] = new ModelPolygon(faces.get(3), faces.get(5), false);
                    ModelPolygon[2] = new ModelPolygon(faces.get(6), faces.get(8), false);
                }
                else
                {
                    ModelPolygon[0] = new ModelPolygon(faces.get(0));
                    ModelPolygon[1] = new ModelPolygon(faces.get(3));
                    ModelPolygon[2] = new ModelPolygon(faces.get(6));
                }
            }
            else if(foundVertex && foundTexel)
            {
                if(loadVertex && loadTexel)
                {
                    ModelPolygon[0] = new ModelPolygon(faces.get(0), faces.get(1), true);
                    ModelPolygon[1] = new ModelPolygon(faces.get(3), faces.get(3), true);
                    ModelPolygon[2] = new ModelPolygon(faces.get(6), faces.get(5), true);
                }
                else
                {
                    ModelPolygon[0] = new ModelPolygon(faces.get(0));
                    ModelPolygon[1] = new ModelPolygon(faces.get(3));
                    ModelPolygon[2] = new ModelPolygon(faces.get(6));
                }
            }
            else if(foundVertex && foundNormal)
            {
                if(loadVertex && loadNormal)
                {
                    ModelPolygon[0] = new ModelPolygon(faces.get(0), faces.get(1), false);
                    ModelPolygon[1] = new ModelPolygon(faces.get(2), faces.get(3), false);
                    ModelPolygon[2] = new ModelPolygon(faces.get(4), faces.get(5), false);
                }
                else
                {
                    ModelPolygon[0] = new ModelPolygon(faces.get(0));
                    ModelPolygon[1] = new ModelPolygon(faces.get(3));
                    ModelPolygon[2] = new ModelPolygon(faces.get(6));
                }
            }
            else if(foundVertex)
            {
                ModelPolygon[0] = new ModelPolygon(faces.get(0));
                ModelPolygon[1] = new ModelPolygon(faces.get(1));
                ModelPolygon[2] = new ModelPolygon(faces.get(2));
            }
            else
            {
                // Error
                System.out.println("The ModelPolygon cannot be processed because no vertex, normal or" +
                        " texel data exists");
            }

            // Add To Faces Array
            fArray.add(ModelPolygon[0]);
            fArray.add(ModelPolygon[1]);
            fArray.add(ModelPolygon[2]);
        }
    }

    public int getResourceId()
    {
        return this.resourceId;
    }
    public boolean isVerticesLoaded() { return this.verticesLoaded; }
    public boolean isTexelsLoaded() { return this.texelsLoaded; }
    public boolean isNormalsLoaded() { return this.normalsLoaded; }
    public int getVertexCount() { return this.vertexCount; }
    public AllowedData getAllowedData() { return this.allowedData; }
}