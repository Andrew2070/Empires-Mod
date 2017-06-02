package com.andrew2070.Empires.API.commands;


import com.andrew2070.Empires.API.commands.ChatComponentContainer;
import com.andrew2070.Empires.API.commands.ChatComponentFormatted;

import net.minecraft.util.IChatComponent;
import com.andrew2070.Empires.Empires;
import com.andrew2070.Empires.entities.Empire.Empire;

import java.util.ArrayList;
import java.util.List;

import com.andrew2070.Empires.entities.Empire.Empire;

public class ChatComponentEmpireList extends ChatComponentMultiPage {
    private Empire.Container empires;

    public ChatComponentEmpireList(Empire.Container empires) {
        super(9);
        this.empires = empires;
        this.construct();
    }

    private void construct() {
        for (Empire t : empires) {
            this.add(new ChatComponentFormatted("{7| - }{%s}", t.toChatMessage()));
        }
    }

    @Override
    public ChatComponentContainer getHeader(int page) {
        ChatComponentContainer header = super.getHeader(page);

        header.add(new ChatComponentFormatted("{9| - Empires}"));

        return header;
    }
}