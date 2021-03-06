package schmoller.tubes.gui;

import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import schmoller.tubes.ModTubes;
import schmoller.tubes.api.gui.GuiExtContainer;
import schmoller.tubes.api.helpers.CommonHelper;
import schmoller.tubes.definitions.TypeRoutingTube;
import schmoller.tubes.network.packets.ModPacketSetRoutingOptions;
import schmoller.tubes.types.RoutingTube;
import schmoller.tubes.types.RoutingTube.RouteDirection;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StatCollector;

public class RoutingTubeGui extends GuiExtContainer
{
	private RoutingTube mTube;
	public RoutingTubeGui(RoutingTube tube, EntityPlayer player)
	{
		super(new RoutingTubeContainer(tube, player));
		
		mTube = tube;
		
		xSize = 176;
		ySize = 220;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer( int curX, int curY )
	{
		String s = StatCollector.translateToLocal("tubes.routing.name");
		fontRendererObj.drawString(s, xSize / 2 - fontRendererObj.getStringWidth(s) / 2, 6, 0x404040);
		fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, this.ySize - 96 + 2, 0x404040);
        
        super.drawGuiContainerForegroundLayer(curX, curY);

		int xx = curX - (width - xSize) / 2;
		int yy = curY - (height - ySize) / 2;

		
		for(int i = 0; i < 9; ++i)
		{
			if(xx >= 9 + (i * 18) && xx <= 23 + (i * 18))
			{
				if(yy >= 92 && yy <= 106) // Direction button
				{
					String text = mTube.getDirection(i).toString();

					drawHoveringText(Arrays.asList(text), xx, yy, fontRendererObj);
					RenderHelper.enableGUIStandardItemLighting();
				}
				else if(yy >= 107 && yy <= 121) // Colour button
				{
					int colour = mTube.getColour(i);
					String text = StatCollector.translateToLocal("gui.colors.none");
					if(colour != -1)
						text = CommonHelper.getDyeName(colour);
					
					drawHoveringText(Arrays.asList(text), xx, yy, fontRendererObj);
					RenderHelper.enableGUIStandardItemLighting();
				}
			}
		}
	}
	
	@Override
	protected void mouseClicked( int x, int y, int button )
	{
		int xx = x - (width - xSize) / 2;
		int yy = y - (height - ySize) / 2;
		
		for(int i = 0; i < 9; ++i)
		{
			if(xx >= 9 + (i * 18) && xx <= 23 + (i * 18))
			{
				if(yy >= 92 && yy <= 106) // Direction button
				{
					int dir = mTube.getDirection(i).ordinal();
					
					if(button == 0)
						++dir;
					else if(button == 1)
						--dir;
					else if(button == 2)
						dir = 7;
					
					if(dir < 0)
						dir = 7;
					if(dir > 7)
						dir = 0;
					
					mTube.setDirection(i, RouteDirection.from(dir));
					ModTubes.packetManager.sendPacketToServer(new ModPacketSetRoutingOptions(mTube.x(), mTube.y(), mTube.z(), i, RouteDirection.from(dir)));
				}
				else if(yy >= 107 && yy <= 121) // Colour button
				{
					int colour = mTube.getColour(i);
					
					if(button == 0)
						++colour;
					else if(button == 1)
						-- colour;
					else if(button == 2)
						colour = -1;
					
					if(colour > 15)
						colour = -1;
					if(colour < -1)
						colour = 15;
					
					mTube.setColour(i, colour);
					
					ModTubes.packetManager.sendPacketToServer(new ModPacketSetRoutingOptions(mTube.x(), mTube.y(), mTube.z(), i, colour));
				}
			}
		}
		
		super.mouseClicked(x, y, button);
	}
	
	
	
	@Override
	protected void drawGuiContainerBackgroundLayer( float f, int i, int j )
	{
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		
		mc.renderEngine.bindTexture(TypeRoutingTube.gui);
		
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
		
		for(int col = 0; col < 9; ++col)
		{
			drawTexturedModalRect(x + 9 + (col * 18), y + 92, 176, mTube.getDirection(col).ordinal() * 14, 14, 14);
			
			int colour = mTube.getColour(col);
			
			if(colour != -1)
			{
				drawRect(x + 12 + (col * 18), y + 110, x + 20 + (col * 18), y + 118, CommonHelper.getDyeColor(colour));
				GL11.glColor4f(1f, 1f, 1f, 1f);
			}
		}
	}

}
