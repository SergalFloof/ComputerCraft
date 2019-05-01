/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.computer.blocks;

import net.minecraft.util.IStringSerializable;

public enum ComputerState implements IStringSerializable
{
    Off( "off" ),
    On( "on" ),
    Blinking( "blinking" );

    private String m_name;

    private ComputerState( String name )
    {
        m_name = name;
    }

    @Override
    public String getName()
    {
        return m_name;
    }

    @Override
    public String toString()
    {
        return getName();
    }
}

