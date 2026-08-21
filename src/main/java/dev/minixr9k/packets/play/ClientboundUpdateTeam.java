package dev.minixr9k.packets.play;

import dev.minixr9k.types.team.Team;
import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import java.util.List;

import static dev.minixr9k.utils.ProtocolUtils.*;

public class ClientboundUpdateTeam implements MinecraftPacket {

    private final Team team;
    private final Team.TeamAction action;
    private final List<String> entities;

    public ClientboundUpdateTeam(Team team, Team.TeamAction action, List<String> entities) {
        this.team = team;
        this.action = action;
        this.entities = entities;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeString(out, team.getName());
        out.writeByte(action.getId());

        switch (action) {
            case CREATE_TEAM -> {
                writeTextComponent(out, team.getDisplayName());
                out.writeByte(team.getFriendlyFlags());

                writeVarInt(out, team.getNametagVisibility());
                writeVarInt(out, team.getCollisionRule());
                writeVarInt(out, team.getColor());

                writeTextComponent(out, team.getPrefix());
                writeTextComponent(out, team.getSuffix());

                writeVarInt(out, team.getEntities().size());
                for (String entityName : team.getEntities()) {
                    writeString(out, entityName);
                }
            }
            case REMOVE_TEAM -> {
                // nothing
            }
            case UPDATE_TEAM_INFO -> {
                writeTextComponent(out, team.getDisplayName());
                out.writeByte(team.getFriendlyFlags());

                writeVarInt(out, team.getNametagVisibility());
                writeVarInt(out, team.getCollisionRule());
                writeVarInt(out, team.getColor());

                writeTextComponent(out, team.getPrefix());
                writeTextComponent(out, team.getSuffix());
            }
            case ADD_ENTITIES, REMOVE_ENTITIES -> {
                writeVarInt(out, entities.size());
                for (String entityName : entities) {
                    writeString(out, entityName);
                }
            }
        }
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x66;
    }
}
