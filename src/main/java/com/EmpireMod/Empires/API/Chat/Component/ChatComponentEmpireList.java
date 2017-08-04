package com.EmpireMod.Empires.API.Chat.Component;


import com.EmpireMod.Empires.entities.Empire.Empire;

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