package dev.ebullient.dnd;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import dev.ebullient.dnd.beastiary.Beastiary;
import dev.ebullient.dnd.combat.Encounter;
import dev.ebullient.dnd.combat.RoundResult;
import dev.ebullient.dnd.combat.TargetSelector;
import dev.ebullient.dnd.mechanics.Dice;

@Path("/combat")
public class EncounterResource {

    final Beastiary beastiary;
    final CombatMetrics metrics;

    EncounterResource(BeastiaryConfig config, CombatMetrics metrics) {
        this.beastiary = config.getBeastiary();
        this.metrics = metrics;
    }

    @GET
    @Path("/any")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RoundResult> any() {
        return go(Dice.range(5) + 2);
    }

    @GET
    @Path("/faceoff")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RoundResult> faceoff() {
        return go(2);
    }

    @GET
    @Path("/melee")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RoundResult> melee() {
        return go(Dice.range(4) + 3);
    }

    List<RoundResult> go(int howMany) {

        Encounter encounter = beastiary.buildEncounter()
                .setHowMany(howMany)
                .setTargetSelector(pickOne(howMany))
                .build();

        List<RoundResult> results = new ArrayList<>();

        while (!encounter.isFinal()) {

            RoundResult result = encounter.oneRound();
            metrics.endRound(result);

            results.add(result);
        }
        metrics.endEncounter(encounter, results.size());

        return results;
    }

    TargetSelector pickOne(int howMany) {
        int which = Dice.range(5);
        switch (which) {
            case 4:
                return TargetSelector.SelectBiggest;
            case 3:
                return TargetSelector.SelectSmallest;
            case 2:
                return TargetSelector.SelectByHighestRelativeHealth;
            case 1:
                return TargetSelector.SelectByLowestRelativeHealth;
            default:
            case 0:
                return TargetSelector.SelectAtRandom;
        }
    }
}