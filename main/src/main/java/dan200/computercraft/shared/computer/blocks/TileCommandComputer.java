/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.computer.blocks;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.apis.CommandAPI;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.ServerComputer;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class TileCommandComputer extends TileComputer
{
    public class CommandSender extends CommandBlockLogic
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
        public ITextComponent getDisplayName()
        {
            IComputer computer = TileCommandComputer.this.getComputer();
            if( computer != null )
            {
                String label = computer.getLabel();
                if( label != null )
                {
                    return new TextComponentString( computer.getLabel() );
                }
            }
            return new TextComponentString( "@" );
        }

        @Override
        public void addChatMessage( ITextComponent chatComponent )
        {
            m_outputTable.put( m_outputTable.size() + 1, chatComponent.getUnformattedText() );
        }

        @Override
        public boolean canCommandSenderUseCommand( int level, String command )
        {
            return level <= 2;
        }

        @Override
        public BlockPos getPosition()
        {
            return TileCommandComputer.this.getPos();
        }

        @Override
        public Vec3d getPositionVector()
        {
            BlockPos pos = getPosition();
            return new Vec3d( pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5 );
        }

        @Override
        public World getEntityWorld()
        {
            return TileCommandComputer.this.world;
        }

        @Override
        public Entity getCommandSenderEntity()
        {
            return null;
        }

        // CommandBlockLogic members intentionally left empty
        // The only reason we extend it at all is so that "gameRule commandBlockOutput" applies to us

        @Override
        public void updateCommand()
        {
        }

        @Override
        public int getCommandBlockType()
        {
            return 0;
        }

        @Override
        public void fillInInfo( ByteBuf buf )
        {
        }
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
    public boolean isUsable( EntityPlayer player, boolean ignoreRange, WorldServer world )
    {
        MinecraftServer server = world.getServer();
        if( server == null || !server.isCommandBlockEnabled() )
        {
            player.addChatMessage( new ChatComponentTranslation( "advMode.notEnabled" ) );
            return false;
        }
        else if( ComputerCraft.canPlayerUseCommands( player ) && player.capabilities.isCreativeMode )
        {
            return super.isUsable( player, ignoreRange );
        }
        else
        {
            player.addChatMessage( new ChatComponentTranslation( "advMode.notAllowed" ) );
            return false;
        }
    }
}
