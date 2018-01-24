package com.avygeil.bprnt.bot;

import java.util.ArrayList;
import java.util.List;

import com.avygeil.bprnt.module.Module;
import com.avygeil.bprnt.module.test.TestModule;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class Bot {
	
	private List<Module> moduleInstances = new ArrayList<>();
	
	public Bot() {
		
		/*
		 * TODO
		 * pour l'instant, je crée l'instance du module manuellement ici,
		 * mais l'idée est que dans le futur, dans ce constructeur, les modules
		 * soit découverts + instanciés automatiquement selon la config associée
		 * à cette instance de bot pour cette guild dans le fichier de config
		 * 
		 * du coup, cette classe sert de relai entre les modules et l'api discord
		 * (elle envoie les events, et plus tard elle permettra au module d'enregistrer
		 * par exemple des commandes)
		 */
		
		moduleInstances.add(new TestModule());
	}
	
	public void onMessageReceived(IUser author, IChannel channel, IMessage message) {
		for (Module module : moduleInstances) {
			module.handleMessage(author, channel, message);
		}
	}

}
