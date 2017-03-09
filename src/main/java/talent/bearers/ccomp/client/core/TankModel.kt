package talent.bearers.ccomp.client.core

import com.google.common.collect.ImmutableList
import com.sun.corba.se.impl.util.RepositoryId.cache
import com.teamwizardry.librarianlib.common.util.ItemNBTHelper
import gnu.trove.map.hash.TIntObjectHashMap
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.*
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.world.World
import net.minecraftforge.client.model.IPerspectiveAwareModel
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad
import net.minecraftforge.client.model.pipeline.VertexTransformer
import net.minecraftforge.common.model.TRSRTransformation
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack
import org.lwjgl.util.vector.Vector3f
import talent.bearers.ccomp.common.core.FluidStack
import java.util.*
import javax.vecmath.Matrix4f
import javax.vecmath.Vector4f
import org.apache.commons.lang3.tuple.Pair as ApachePair


/**
 * @author WireSegal
 * Created at 2:38 PM on 3/8/17.
 */
class TankModel(val original: IBakedModel) : IBakedModel by original {

    val overrideList = object : ItemOverrideList(listOf()) {
        override fun handleItemState(originalModel: IBakedModel, stack: ItemStack, world: World?, entity: EntityLivingBase?): IBakedModel {
            if (stack.hasTagCompound() && ItemNBTHelper.verifyExistence(stack, "fluid")) {
                val fluid = FluidStack(ItemNBTHelper.getCompound(stack, "fluid", false)!!)
                return getModel(fluid)
            }
            return super.handleItemState(originalModel, stack, world, entity)
        }
    }

    override fun getOverrides(): ItemOverrideList = overrideList

    private fun getModel(stack: FluidStack): CompositeBakedModel {
        val amount = stack.amount
        val clamped = amount / 125

        val key = clamped to stack.fluid

        var model = cache[key]
		if(model == null) {
            val parent = FluidModel(stack, clamped)
			model = CompositeBakedModel(parent, original)
			cache.put(key, model)
		}
		return model
	}

    private inner class FluidModel(fluid: FluidStack, amount: Int) : IBakedModel {
        val still: TextureAtlasSprite = Minecraft.getMinecraft().textureMapBlocks
                .getTextureExtry(fluid.fluid.getStill(fluid).toString())
        val flowing: TextureAtlasSprite = Minecraft.getMinecraft().textureMapBlocks
                .getTextureExtry(fluid.fluid.getFlowing(fluid).toString())


        val minSize = 2.02f
        val maxSize = 13.98f
        val baseHeight = 0.02f

        val actualHeight = amount.toFloat() / 32
        val height = (actualHeight) * 16 - baseHeight

        val minCorner = Vector3f(minSize, baseHeight, minSize)
        val xCorner = Vector3f(maxSize, baseHeight, minSize)
        val zCorner = Vector3f(minSize, baseHeight, maxSize)
        val maxCorner = Vector3f(maxSize, baseHeight, maxSize)
        val minTopCorner = Vector3f(minSize, height, minSize)
        val xTopCorner = Vector3f(maxSize, height, minSize)
        val zTopCorner = Vector3f(minSize, height, maxSize)
        val maxTopCorner = Vector3f(maxSize, height, maxSize)

        val quads: List<BakedQuad>

        init {
            val builder = ImmutableList.builder<BakedQuad>()
            val bakery = FaceBakery()
            createSide(builder, bakery, EnumFacing.DOWN, still, minCorner, maxCorner)
            createSide(builder, bakery, EnumFacing.UP, still, minTopCorner, maxTopCorner)
            createSide(builder, bakery, EnumFacing.NORTH, flowing, minCorner, xTopCorner)
            createSide(builder, bakery, EnumFacing.SOUTH, flowing, zCorner, maxTopCorner)
            createSide(builder, bakery, EnumFacing.WEST, flowing, minCorner, zTopCorner)
            createSide(builder, bakery, EnumFacing.EAST, flowing, xCorner, maxTopCorner)
            quads = builder.build()
        }

        fun createSide(builder: ImmutableList.Builder<BakedQuad>, bakery: FaceBakery, side: EnumFacing, tex: TextureAtlasSprite, min: Vector3f, max: Vector3f) {
            val defUVs = if (side.horizontalIndex == -1) floatArrayOf(minSize, minSize, maxSize, maxSize)
            else floatArrayOf(minSize, baseHeight, maxSize, height)
            val uv = BlockFaceUV(defUVs, 0)
            val bpf = BlockPartFace(null, 0, "", uv)
            builder.add(bakery.makeBakedQuad(min, max, bpf, tex, side, ModelRotation.X0_Y0, null, false, false))
        }

        override fun getParticleTexture(): TextureAtlasSprite = still

        override fun getQuads(state: IBlockState?, side: EnumFacing?, rand: Long): List<BakedQuad> = quads

        override fun getItemCameraTransforms(): ItemCameraTransforms = this@TankModel.itemCameraTransforms

        override fun isBuiltInRenderer() = false
        override fun isAmbientOcclusion() = original.isAmbientOcclusion
        override fun isGui3d() = true

        override fun getOverrides(): ItemOverrideList = ItemOverrideList.NONE
    }

    companion object {
        private val cache = HashMap<Pair<Int, Fluid>, CompositeBakedModel>()

        private fun transform(quad: BakedQuad, transform: TRSRTransformation): BakedQuad {
            val builder = UnpackedBakedQuad.Builder(DefaultVertexFormats.ITEM)
            val consumer = object : VertexTransformer(builder) {
                override fun put(element: Int, vararg data: Float) {
                    val formatElement = DefaultVertexFormats.ITEM.getElement(element)
                    when (formatElement.usage) {
                        POSITION -> {
                            val newData = FloatArray(4)
                            val vec = Vector4f(data)
                            transform.matrix.transform(vec)
                            vec.get(newData)
                            parent.put(element, *newData)
                        }
                        else -> parent.put(element, *data)
                    }
                }
            }
            quad.pipe(consumer)
            return builder.build()
        }
    }

    private class CompositeBakedModel(toAdd: IBakedModel, private val parent: IBakedModel) : IPerspectiveAwareModel, IBakedModel by parent {
        private val genQuads: List<BakedQuad>
        private val faceQuads = EnumMap<EnumFacing, MutableList<BakedQuad>>(EnumFacing::class.java)

        init {
            val genBuilder = ImmutableList.builder<BakedQuad>()
            val transform = TRSRTransformation.blockCenterToCorner(TRSRTransformation(javax.vecmath.Vector3f(-0.4f, 0.25f, 0f), null, javax.vecmath.Vector3f(0.625f, 0.625f, 0.625f), TRSRTransformation.quatFromXYZ(0f, Math.PI.toFloat() / 2, 0f)))

            for (e in EnumFacing.VALUES)
                faceQuads.put(e, ArrayList())

            toAdd.getQuads(null, null, 0).forEach {
                genBuilder.add(transform(it, transform))
            }

            for (e in EnumFacing.VALUES) {
                faceQuads[e]?.addAll(toAdd.getQuads(null, e, 0).map({ input -> transform(input, transform) }))
            }

            genBuilder.addAll(parent.getQuads(null, null, 0))
            for (e in EnumFacing.VALUES) {
                faceQuads[e]?.addAll(parent.getQuads(null, e, 0))
            }

            genQuads = genBuilder.build()
        }

        override fun getQuads(state: IBlockState?, face: EnumFacing?, rand: Long): List<BakedQuad> {
            return if (face == null) genQuads else faceQuads[face]!!
        }

        override fun getOverrides(): ItemOverrideList = ItemOverrideList.NONE

        override fun handlePerspective(cameraTransformType: ItemCameraTransforms.TransformType): ApachePair<IBakedModel, Matrix4f> {
            if (parent is IPerspectiveAwareModel) {
                val pair = parent.handlePerspective(cameraTransformType)
                if (pair != null && pair.right != null)
                    return ApachePair.of(this, pair.right)
            }
            return ApachePair.of(this, TRSRTransformation.identity().matrix)
        }
    }
}
