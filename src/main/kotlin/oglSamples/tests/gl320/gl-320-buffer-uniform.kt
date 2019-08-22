//package oglSamples.tests.gl320
//
//import glm_.BYTES
//import glm_.glm
//import glm_.mat3x3.Mat3
//import glm_.mat4x4.Mat4
//import glm_.vec3.Vec3
//import glm_.vec4.Vec4
//import gln.BufferTarget.Companion.ARRAY
//import gln.BufferTarget.Companion.ELEMENT_ARRAY
//import gln.BufferTarget.Companion.UNIFORM
//import gln.GlBufferEnum
//import gln.Usage.Companion.DYNAMIC_DRAW
//import gln.cap.Caps
//import gln.clear.glClearBuffer
//import gln.glViewport
//import gln.glf.glf
//import gln.objects.GlBuffers
//import gln.objects.GlProgram
//import gln.vertexArray.GlVertexArray
//import gln.vertexArray.glEnableVertexAttribArray
//import gln.vertexArray.glVertexAttribPointer
//import kool.shortBufferOf
//import kool.use
//import oglSamples.Vertex_v3n3c4
//import oglSamples.framework.Framework
//import oglSamples.framework.semantic
//import oglSamples.vertex_v3n3c4_buffer_of
//import org.lwjgl.opengl.GL11C.GL_COLOR
//import org.lwjgl.opengl.GL11C.GL_DEPTH
//import org.lwjgl.opengl.GL30C.GL_MAP_INVALIDATE_BUFFER_BIT
//import org.lwjgl.opengl.GL30C.GL_MAP_WRITE_BIT
//import uno.buffer.bufferOf
//import java.nio.ByteBuffer
//
//fun main() {
//    gl_320_buffer_uniform()
//}
//
//private class gl_320_buffer_uniform : Framework("gl-320-buffer-uniform", Caps.Profile.CORE, 3, 2) {
//
//    val SHADER_SOURCE = "gl-320/buffer-uniform"
//
//    val vertexCount = 4
//    val vertexSize = vertexCount * Vertex_v3n3c4.size
//    val vertexData = vertex_v3n3c4_buffer_of(
//            Vertex_v3n3c4(Vec3(-1f, -1f, 0f), Vec3(0f, 0f, 1f), Vec4(1f, 0f, 0f, 1f)),
//            Vertex_v3n3c4(Vec3(+1f, -1f, 0f), Vec3(0f, 0f, 1f), Vec4(0f, 1f, 0f, 1f)),
//            Vertex_v3n3c4(Vec3(+1f, 1f, 0f), Vec3(0f, 0f, 1f), Vec4(0f, 0f, 1f, 1f)),
//            Vertex_v3n3c4(Vec3(-1f, +1f, 0), Vec3(0f, 0f, 1f), Vec4(1f, 1f, 1f, 1f)))
//
//    val elementCount = 6
//    val elementSize = elementCount * Short.BYTES
//    val elementData = shortBufferOf(
//            0, 1, 2,
//            2, 3, 0)
//
///*
//	GLsizei const vertexCount(4);
//	GLsizeiptr const VertexSize = vertexCount * sizeof(vertex_v3fn3fc4f);
//	vertex_v3fn3fc4f const VertexData[vertexCount] =
//	{
//		vertex_v3fn3fc4f(glm::vec3(-1.000f, -0.732f, -0.732f), glm::normalize(glm::vec3(-1.000f, -0.732f, -0.732f)), glm::vec4(1.0f, 0.0f, 0.0f, 1.0f)),
//		vertex_v3fn3fc4f(glm::vec3( 1.000f, -0.732f, -0.732f), glm::normalize(glm::vec3( 1.000f, -0.732f, -0.732f)), glm::vec4(0.0f, 1.0f, 0.0f, 1.0f)),
//		vertex_v3fn3fc4f(glm::vec3( 0.000f,  1.000f, -0.732f), glm::normalize(glm::vec3( 0.000f,  1.000f, -0.732f)), glm::vec4(0.0f, 0.0f, 1.0f, 1.0f)),
//		vertex_v3fn3fc4f(glm::vec3( 0.000f,  0.000f,  1.000f), glm::normalize(glm::vec3( 0.000f,  0.000f,  1.000f)), glm::vec4(1.0f, 1.0f, 1.0f, 1.0f))
//	};
//
//	GLsizei const ElementCount(12);
//	GLsizeiptr const ElementSize = ElementCount * sizeof(GLushort);
//	GLushort const ElementData[ElementCount] =
//	{
//		0, 2, 1,
//		0, 1, 3,
//		1, 2, 3,
//		2, 0, 3
//	};
//*/
//
//    enum class Buffer : GlBufferEnum { VERTEX, ELEMENT, PER_SCENE, PER_PASS, PER_DRAW }
//
//    enum class Uniform { PER_SCENE, PER_PASS, PER_DRAW, LIGHT }
//
//    class Material(val ambient: Vec3, //padding1: Float
//                   val diffuse: Vec3, //padding2: Float
//                   val specular: Vec3,
//                   val shininess: Float) {
//
//        fun toBuffer() = bufferOf(ambient, diffuse, specular, shininess)
//    }
//
//    object transform {
//        lateinit var buffur: ByteBuffer
//
//        var p = Mat4()
//            set(value) {
//                value to buffur
//                field = value
//            }
//        var mv = Mat4()
//            set(value) {
//                value.to(buffur, Mat4.size)
//                field = value
//            }
//        var normal = Mat3()
//            set(value) {
//                value.to(buffur, Mat4.size * 2)
//                field = value
//            }
//
//        val size = Mat4.size * 2 + Mat3.size
//    }
//
//    var program = GlProgram.NULL
//    var vertexArray = GlVertexArray()
//    var buffers = GlBuffers<Buffer>()
//    var uniformPerDraw = -1
//    var uniformPerPass = -1
//    var uniformPerScene = -1
//
//    override fun begin(): Boolean {
//
//        var validated = true
//
//        if (validated)
//            validated = initProgram()
//        if (validated)
//            validated = initBuffer()
//        if (validated)
//            validated = initVertexArray()
//
//        glEnable(GL_DEPTH_TEST)
//        glBindFramebuffer(GL_FRAMEBUFFER, 0)
//        glDrawBuffer(GL_BACK)
//        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
//            return false
//
//        return validated
//    }
//
//    fun initProgram(): Boolean {
//
//        var validated = true
//
//        try {
//            program = GlProgram.initFromPath("$SHADER_SOURCE.vert", "$SHADER_SOURCE.frag") {
//
//                "Position".attrib = semantic.attr.POSITION
//                "Normal".attrib = semantic.attr.NORMAL
//                "Color".attrib = semantic.attr.COLOR
//                "Color".fragData = semantic.frag.COLOR
//            }
//        } catch (exc: Exception) {
//            validated = false
//        }
//
//        if (validated)
//            program.apply {
//                uniformPerDraw = getUniformBlockIndex("per_draw")
//                uniformPerPass = getUniformBlockIndex("per_pass")
//                uniformPerScene = getUniformBlockIndex("per_scene")
//
//                uniformBlockBinding(uniformPerDraw, Uniform.PER_DRAW)
//                uniformBlockBinding(uniformPerPass, Uniform.PER_PASS)
//                uniformBlockBinding(uniformPerScene, Uniform.PER_SCENE)
//            }
//        return validated
//    }
//
//    fun initVertexArray(): Boolean {
//
//        vertexArray = GlVertexArray.gen().bound {
//
//            buffers {
//
//                Buffer.VERTEX.bound(ARRAY) {
//                    glVertexAttribPointer(glf.pos3_nor3_col4)
//                }
//
//                glEnableVertexAttribArray(glf.pos3_nor3_col4)
//
//                Buffer.ELEMENT.bind(ELEMENT_ARRAY)
//            }
//        }
//
//        return true
//    }
//
//    fun initBuffer(): Boolean {
//
//        buffers.gen {
//
//            Buffer.ELEMENT.bound(ELEMENT_ARRAY) { data(elementData) }
//
//            Buffer.VERTEX.bound(ARRAY) { data(vertexData.data) }
//
//            Buffer.PER_DRAW.bound(UNIFORM) { data(transform.size, DYNAMIC_DRAW) }
//
//            Buffer.PER_PASS.bound(UNIFORM) {
//                val light = Vec3(0f, 0f, 100f)
//                data(light)
//            }
//
//            Buffer.PER_SCENE.bound(UNIFORM) {
//                val material = Material(Vec3(0.7f, 0f, 0f), Vec3(0f, 0.5f, 0f), Vec3(0f, 0f, 0.5f), 128f)
//                material.toBuffer().use {
//                    data(it)
//                }
//            }
//        }
//        return true
//    }
//
//    override fun render(): Boolean {
//
//        buffers[Buffer.PER_DRAW].bound(UNIFORM) {
//
//            mappedRange(transform.size, GL_MAP_WRITE_BIT or GL_MAP_INVALIDATE_BUFFER_BIT) { buf ->
//
//                val projection = glm.perspective(glm.πf * 0.25f, window.aspect, 0.1f, 100f)
//                val model = Mat4().rotate(-glm.πf * 0.5f, 0f, 0f, 1f)
//
//                transform.apply {
//                    buffur = buf!!
//                    mv = view * model
//                    p = projection
//                    normal = glm.inverseTranspose(mv).toMat3()
//                }
//            }
//        }
//
//        glViewport(windowSize)
//        glClearBuffer(GL_COLOR, 0, Vec4(0.2f, 0.2f, 0.2f, 1f))
//        glClearBuffer(GL_DEPTH, 0, 1f)
//
//        program.use()
//        buffers {
//            Buffer.PER_SCENE.bindBase(UNIFORM, semantic.uniform.PER_SCENE)
//            Buffer.PER_SCENE.bindBase(UNIFORM, semantic.uniform.PER_SCENE)
//        }
//        glBindBufferBase(GL_UNIFORM_BUFFER, uniform::PER_SCENE, BufferName[buffer::PER_SCENE])
//        glBindBufferBase(GL_UNIFORM_BUFFER, uniform::PER_PASS, BufferName[buffer::PER_PASS])
//        glBindBufferBase(GL_UNIFORM_BUFFER, uniform::PER_DRAW, BufferName[buffer::PER_DRAW])
//        glBindVertexArray(vertexArray)
//
//        glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, nullptr, 1, 0)
//
//        return true
//    }
//
//    override fun end(): Boolean {
//        vertexArray.delete()
//        buffers.del
//        glDeleteBuffers(buffer::MAX, & BufferName [0])
//        glDeleteProgram(program)
//
//        return true
//    }
//
//
//}