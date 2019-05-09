/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.computer.blocks;

import java.util.HashMap;
import java.util.Map;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.apis.CommandAPI;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextComponentTranslation;

public class TileCommandComputer extends TileComputer
{
    public class CommandSender extends CommandBase
    {
        private Map<Integer, String> m_outputTable;

        public CommandSender()
        {
            m_outputTable = new HashMap<Integer, String>();
        }

        public void clearOutput()
        {
            m_outputTable.clear();
        }

        public Map<Integer, String> getOutput()
        {
            return m_outputTable;
        }

        public Map<Integer, String> copyOutput()
        {
            return new HashMap<Integer, String>( m_outputTable );
        }

        // ICommandSender


		@Override
		public String getName() {
			IComputer computer = TileCommandComputer.this.getComputer();
            if( computer != null )
            {
                String label = computer.getLabel();
                if( label != null )
                {
                    return new String( computer.getLabel() );
                }
            }
            return new String( "@" );
		}

		@Override
		public String getUsage(ICommandSender sender) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
			return true;
		}

//        @Override
//        public void addChatMessage( ITextComponent chatComponent )
//        {
//            m_outputTable.put( m_outputTable.size() + 1, chatComponent.getUnformattedText() );
//        }
//
//        @Override
//        public BlockPos getPosition()
//        {
//            return TileCommandComputer.this.getPos();
//        }
//
//        @Override
//        public Vec3d getPositionVector()
//        {
//            BlockPos pos = getPosition();
//            return new Vec3d( pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5 );
//        }
//
//        @Override
//        public World getEntityWorld()
//        {
//            return TileCommandComputer.this.world;
//        }
//
//        @Override
//        public Entity getCommandSenderEntity()
//        {
//            return null;
//        }

        // CommandBlockLogic members intentionally left empty
        // The only reason we extend it at all is so that "gameRule commandBlockOutput" applies to us

//        @Override
//        public void updateCommand()
//        {
//        }
//
//        @Override
//        public int getCommandBlockType()
//        {
//            return 0;
//        }
//
//        @Override
//        public void fillInInfo( ByteBuf b )
//        {
//        }
    }

    private CommandSender m_commandSender;

    public TileCommandComputer()
    {
        m_commandSender = new CommandSender();
    }

    @Override
    public EnumFacing getDirection()
    {
        IBlockState state = getBlockState();
        return (EnumFacing)state.getValue( BlockCommandComputer.Properties.FACING );
    }

    @Override
    public void setDirection( EnumFacing dir )
    {
        if( dir.getAxis() == EnumFacing.Axis.Y )
        {
            dir = EnumFacing.NORTH;
        }
        setBlockState( getBlockState().withProperty( BlockCommandComputer.Properties.FACING, dir ) );
        updateInput();
    }

    public CommandSender getCommandSender()
    {
        return m_commandSender;
    }

    @Override
    protected ServerComputer createComputer( int instanceID, int id )
    {
        ServerComputer computer = super.createComputer( instanceID, id );
        computer.addAPI( new CommandAPI( this ) );
        return computer;
    }

    @Override
    public boolean isUsable( EntityPlayer player, boolean ignoreRange )
    {
        MinecraftServer server = world.getMinecraftServer();
        if( server == null || !server.isCommandBlockEnabled() )
        {
            player.sendMessage( new TextComponentTranslation( "advMode.notEnabled" ) );
            return false;
        }
        else if( ComputerCraft.canPlayerUseCommands( player, world ) && player.capabilities.isCreativeMode )
        {
            return super.isUsable( player, ignoreRange );
        }
        else
        {
            player.sendMessage( new TextComponentTranslation( "advMode.notAllowed" ) );
            return false;
        }
    }
}
