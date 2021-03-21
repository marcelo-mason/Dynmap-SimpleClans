package net.sacredlabyrinth.phaed.dynmap.simpleclans.entries;


import java.util.Date;

import org.bukkit.Location;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.Helper;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;

public class KillEntry
{
    private final ClanPlayer victim;
    private final ClanPlayer attacker;
    private final Location location;
    private final long timestamp;

    public KillEntry(ClanPlayer victim, ClanPlayer attacker, Location location)
    {
        this.victim = victim;
        this.attacker = attacker;
        this.timestamp = new Date().getTime();
        this.location = location;
    }

    public long getAgeSeconds()
    {	
        return (System.currentTimeMillis() - timestamp) / 1000;
    }

    public ClanPlayer getVictim()
    {
        return victim;
    }

    public ClanPlayer getAttacker()
    {
        return attacker;
    }

    public Location getLocation()
    {
        return location;
    }

    @Override
    public String toString()
    {
        return victim.getName() + "." + attacker.getName() + "." + Helper.toLocationString(location);
    }
}
