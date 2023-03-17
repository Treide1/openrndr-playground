package external.chatgpt

//import org.openrndr.application
//import org.openrndr.color.ColorRGBa
//import org.openrndr.draw.*
//import org.openrndr.extra.camera.Orbital
//import org.openrndr.extra.dnk3.DirectionalLight
//import org.openrndr.math.Vector3
//import org.openrndr.shape.Sphere
//
//fun main() = application {
//    configure {
//        width = 640
//        height = 480
//    }
//
//    program {
//        val model = loadWavefrontObj("volume.obj")
//
//        val camera = Orbital()
//        camera.eye = Vector3(0.0, 0.0, 10.0)
//        camera.lookAt = Vector3.ZERO
//
//        val light = DirectionalLight(
//            color = ColorRGBa.WHITE,
//            direction = Vector3.UNIT_Z,
//            intensity = 5.0
//        )
//
//        val shader = shadeStyle {
//            fragmentTransform = """
//                // calculate the color and density of the volume
//                vec4 volumeColor = texture(p_volume, va_texCoord0.st);
//                float density = length(volumeColor.rgb);
//
//                // calculate the light attenuation
//                vec3 rayDirection = normalize(v_worldPosition - lightDirection.xyz);
//                float lightAttenuation = max(dot(rayDirection, normal), 0.0);
//
//                // apply the volumetric lighting effect
//                vec3 color = (1.0 - exp(-density * lightAttenuation)) * volumeColor.rgb;
//                gl_FragColor = vec4(color, 1.0);
//            """
//        }
//
//        val framebuffer = colorBuffer(width, height)
//
//        extend {
//            drawer.withTarget(framebuffer) {
//                clear(ColorRGBa.BLACK)
//                drawer.shadeStyle = shader
//                drawer.shadeParameters["lightDirection"] = light.direction
//                drawer.shadeParameters["p_volume"] = model.textureFilterMinify(
//                    FilterMode.LINEAR_MIPMAP_LINEAR,
//                    FilterMode.LINEAR
//                )
//                drawer.drawStyle.colorMatrix = tint(ColorRGBa.WHITE)
//                drawer.drawModel(model, shading = true)
//            }
//
//            drawer.image(framebuffer)
//        }
//    }
//}
