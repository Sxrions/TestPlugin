package dev.jojo.plugin.duel;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;

public class Duel {
    private Player player1;
    private Player player2;
    private World arena;
    private DuelState state;

    private int countdown = 5;
    private int timer = 120;

    public Duel(Player p1, Player p2, World arena){
        this.player1 = p1;
        this.player2 = p2;
        this.arena = arena;
        this.state = DuelState.STARTING;
        // TODO AMENER LES JOUEURS ETC ptretre avec un methode en + ou mettre dans START, RESERVER l'ARENE (ptetre a mettre dans manager plutot jsp)
    }

    public void start(){
        //TODO MODIFIER ETATS JOUEURS, COUNTDOWN & TIMER scheduler -> les deux sont exec toutes les secondes et check si 0 -> timer call endDraw()

        this.state = DuelState.PLAYING;
    }

    public void end(Player looser){
        this.state = DuelState.ENDING;
        //TODO Affichage victoire défaite puis call end()
    }

    public void endDraw(){
        this.state = DuelState.ENDING;
        //TODO Affichage DRAW puis call end()
    }

    public void end(){
        //TODO RESET LES JOUEURS, RESET l'arene
    } //TODO PAS OUBLIER DE VIRER DUEL DE DUELMANAGER

    enum DuelState{
        STARTING,
        PLAYING,
        ENDING
    }
}
