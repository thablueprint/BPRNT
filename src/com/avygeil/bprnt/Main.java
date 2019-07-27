package com.avygeil.bprnt;

import com.avygeil.bprnt.bot.BotManager;
import com.avygeil.bprnt.util.BaseEmoji;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Main {
	
	public static final Logger LOGGER;
	
	static {
		LOGGER = LoggerFactory.getLogger(Main.class);
	}
	
	public static void main(String[] args) {
		// parse command line arguments
		// TODO: move this code later
		
		final Options options = buildArgumentOptions();
		final CommandLineParser parser = new DefaultParser();
		
		CommandLine cmd = null;
		
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		boolean noStart = false;
		
		final String decodeFilePath = cmd.getOptionValue("emojidecode", "");
		
		if (!decodeFilePath.isEmpty()) {
			noStart = true;

			try {
				final File fileToDecode = new File(decodeFilePath);
				final File decodedFile = new File(FilenameUtils.removeExtension(decodeFilePath) + "_decoded.txt");
				
				final String encodedContent = FileUtils.readFileToString(fileToDecode, StandardCharsets.UTF_8);
				FileUtils.writeStringToFile(decodedFile, BaseEmoji.decode(encodedContent), StandardCharsets.UTF_8);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		final String encodeFilePath = cmd.getOptionValue("emojiencode", "");
		
		if (!encodeFilePath.isEmpty()) {
			noStart = true;

			try {
				final File fileToEncode = new File(encodeFilePath);
				final File encodedFile = new File(FilenameUtils.removeExtension(encodeFilePath) + "_encoded.emoji");
				
				final String contentToEncode = FileUtils.readFileToString(fileToEncode, StandardCharsets.UTF_8);
				FileUtils.writeStringToFile(encodedFile, BaseEmoji.encode(contentToEncode), StandardCharsets.UTF_8);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (noStart) {
			LOGGER.info("The bot was not started due to command line arguments");
			return;
		}
		
		// start the bot manager

		final BotManager botManager = new BotManager();

		try {
			botManager.initialize();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		botManager.start().doOnError(e -> e.printStackTrace()).block();
	}
	
	public static Options buildArgumentOptions() {
		final Option decodeEmoji = Option.builder("d")
				.longOpt("emojidecode")
				.desc("Decodes the specified file from emoji")
				.hasArg(true)
				.required(false)
				.build();
		
		final Option encodeEmoji = Option.builder("e")
				.longOpt("emojiencode")
				.desc("Encodes the specified file to emoji")
				.hasArg(true)
				.required(false)
				.build();
		
		final Options result = new Options();
		result.addOption(decodeEmoji);
		result.addOption(encodeEmoji);
		
		return result;
	}

}
