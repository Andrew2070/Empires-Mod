package com.EmpireMod.Empires.API.JSON.Configuration;


import java.util.ArrayList;
import java.util.List;

import com.EmpireMod.Empires.Empires;
import com.EmpireMod.Empires.entities.Empire.Rank;
import com.google.gson.GsonBuilder;

public class RanksConfig extends JsonConfig<Rank, Rank.Container> {

    public RanksConfig(String path) {
        super(path, "DefaultEmpireRanks");
        this.gsonType = Rank.Container.class;
        this.gson = new GsonBuilder().registerTypeAdapter(Rank.class, new Rank.Serializer()).setPrettyPrinting().create();
    }

    @Override
    protected Rank.Container newList() {
        return new Rank.Container();
    }

    @Override
    public void create(Rank.Container items) {
        Rank.initDefaultRanks();
        items.addAll(Rank.defaultRanks);
        super.create(items);
    }

    @Override
    public Rank.Container read() {
        Rank.Container ranks = super.read();

        Rank.defaultRanks.clear();
        Rank.defaultRanks.addAll(ranks);

        return ranks;
    }

    @Override
    public boolean validate(Rank.Container items) {
        boolean isValid = true;
        for(Rank.Type type : Rank.Type.values()) {
            if(type.unique) {
                List<Rank> rankOfType = new ArrayList<Rank>();
                for(Rank rank : items) {
                    if(rank.getType() == type) {
                        rankOfType.add(rank);
                    }
                }

                if(rankOfType.size() == 0) {
                    isValid = false;
                    Empires.instance.LOG.error("Unique type of Rank was not found in " + name);
                    items.add(Rank.defaultRanks.get(type));
                } else if(rankOfType.size() > 1) {
                    isValid = false;
                    Empires.instance.LOG.error("Unique type of Rank was found multiple times in " + name + ". Setting all aside from the first to type regular.");
                    for(int i = 1; i < rankOfType.size(); i++) {
                        rankOfType.get(i).setType(Rank.Type.OFFICER);
                    }
                }
            }
        }

        return isValid;
    }
}