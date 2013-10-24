package schmoller.tubes.parts;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import schmoller.tubes.ITube;
import schmoller.tubes.ModTubes;
import schmoller.tubes.TubeRegistry;
import schmoller.tubes.definitions.TubeDefinition;
import schmoller.tubes.render.RenderHelper;
import codechicken.core.data.MCDataInput;
import codechicken.core.data.MCDataOutput;
import codechicken.core.lighting.LazyLightMatrix;
import codechicken.core.vec.Cuboid6;
import codechicken.core.vec.Vector3;
import codechicken.multipart.IconHitEffects;
import codechicken.multipart.JCuboidPart;
import codechicken.multipart.JIconHitEffects;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.NormalOcclusionTest;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TSlottedPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class BaseTubePart extends JCuboidPart implements ITube, JNormalOcclusion, JIconHitEffects, TSlottedPart
{
	private String mType;
	private TubeDefinition mDef;
	
	public static final int CHANNEL_DATA = 254;
	public static final int CHANNEL_RENDER = 255;
	
	public BaseTubePart(String type)
	{
		mDef = TubeRegistry.instance().getDefinition(type);
		mType = type;
	}
	
	@Override
	public String getType()
	{
		return "tubes_" + mType;
	}

	@Override
	public Cuboid6 getBounds()
	{
		return mDef.getSize();
	}

	@Override
	public boolean occlusionTest( TMultiPart npart )
	{
		return NormalOcclusionTest.apply(this, npart);
	}
	
	@Override
	public Iterable<Cuboid6> getOcclusionBoxes()
	{
		ArrayList<Cuboid6> boxes = new ArrayList<Cuboid6>();
		boxes.add(getBounds());
		return boxes;
	}
	
	@Override
	public ItemStack pickItem( MovingObjectPosition hit )
	{
		return ModTubes.itemTube.createForType(mType);
	}
	
	@Override
	public final Iterable<ItemStack> getDrops()
	{
		ArrayList<ItemStack> stacks = new ArrayList<ItemStack>(1);
		stacks.add(ModTubes.itemTube.createForType(mType));
		
		onDropItems(stacks);
		
		return stacks;
	}
	
	
	
	@Override
	public void bind( TileMultipart t )
	{
		// Workaround: if the tile changes, value doesTick() is ignored. This makes sure it continues to tick 
		if(tile() != null && tile() != t)
		{
			world().loadedTileEntityList.remove(tile());
			world().addTileEntity(t);
		}
		
		super.bind(t);
	}
	
	@Override
	public boolean doesTick()
	{
		return true;
	}
	
	@Override
	public final void read( MCDataInput packet )
	{
		int id = packet.readUnsignedByte();
		if(id == CHANNEL_DATA)
			readDesc(packet);
		else if(id == CHANNEL_RENDER)
			tile().markRender();
		else
			onRecieveDataClient(id, packet);
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public final void renderDynamic( Vector3 pos, float frame, int pass )
	{
		if(pass == 0)
			RenderHelper.renderDynamic(this, mDef, pos);
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public final void renderStatic(Vector3 pos, LazyLightMatrix olm, int pass) 
	{
		RenderHelper.renderStatic(this, mDef);
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public final void addHitEffects( MovingObjectPosition hit, EffectRenderer effectRenderer )
	{
		IconHitEffects.addHitEffects(this, hit, effectRenderer);
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public final void addDestroyEffects( EffectRenderer effectRenderer )
	{
		IconHitEffects.addDestroyEffects(this, effectRenderer);
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public final Icon getBreakingIcon( Object subPart, int side )
	{
		return getBrokenIcon(side);
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public final Icon getBrokenIcon( int side )
	{
		return mDef.getCenterIcon();
	}

	@Override
	public final int getSlotMask()
	{
		return 1 << 6;
	}

	// ============================================
	// Part Operations for clients to use
	// ============================================
	
	protected void onRecieveDataClient(int channel, MCDataInput input) {};
	
	protected MCDataOutput openChannel(int channel)
	{
		assert(channel < 254);
		return tile().getWriteStream(this).writeByte(channel);
	}
	
	protected void markForUpdate()
	{
		if(world().isRemote)
			return;
		writeDesc(tile().getWriteStream(this).writeByte(CHANNEL_DATA));
	}
	
	protected void markForRender()
	{
		if(world().isRemote)
			tile().markRender();
		else
			tile().getWriteStream(this).writeByte(CHANNEL_RENDER);
	}
	
	protected void onDropItems(List<ItemStack> itemsToDrop) {}
	
}
