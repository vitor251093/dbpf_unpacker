/****************************************************************************
* Copyright (C) 2019 Eric Mor
*
* This file is part of SporeModder FX.
*
* SporeModder FX is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
****************************************************************************/

package sporemodder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import sporemodder.MessageManager.MessageType;

/**
 * The main class of the program.
 */
public class MainApp extends Application {
	
	private static MainApp instance;
	private MessageManager messageManager;
	private PathManager pathManager;
	private HashManager hashManager;
	private ProjectManager projectManager;
	private GameManager gameManager;
	private DocumentationManager documentationManager;
	private FormatManager formatManager;
	private FileManager fileManager;
	private UpdateManager updateManager;

	private Properties settings;
	

	/**
	 * Returns the class that manages internal messages and events.
	 * @return The message manager.
	 */
	public MessageManager getMessageManager() {
		return messageManager;
	}

	/**
	 * Returns the class that manages the program and projects path.
	 * @return The path manager.
	 */
	public PathManager getPathManager() {
		return pathManager;
	}
	
	/**
	 * Returns the class that controls the names and hashes, number formatting, etc.
	 * @return The hash manager.
	 */
	public HashManager getHashManager() {
		return hashManager;
	}
	
	/**
	 * Returns the class that manages the projects in the program.
	 * @return The project manager.
	 */
	public ProjectManager getProjectManager() {
		return projectManager;
	}
	
	/**
	 * Returns the class that manages the available Spore games.
	 * @return The game manager
	 */
	public GameManager getGameManager() {
		return gameManager;
	}
	
	/**
	 * Returns the class that controls the documentation of the program.
	 * @return The documentation manager
	 */
	public DocumentationManager getDocumentationManager() {
		return documentationManager;
	}
	
	/**
	 * Returns the class that keeps track of all the convertible file formats.
	 * @return The format manager
	 */
	public FormatManager getFormatManager() {
		return formatManager;
	}
	
	/**
	 * Returns the class that contains multiple utilities to work with files and folders.
	 * @return The file manager.
	 */
	public FileManager getFileManager() {
		return fileManager;
	}
	
	public UpdateManager getUpdateManager() {
		return updateManager;
	}

	/**
	 * Returns the current instance of the MainApp class.
	 */
	public static MainApp get() {
		return MainApp.instance;
	}
	
	public static void testInit() {
		new MainApp().init(true);
	}
	
	private void init(boolean testInit) {
		MainApp.instance = this;
		
		messageManager = new MessageManager();
		gameManager = new GameManager();
		pathManager = new PathManager();
		hashManager = new HashManager();
		projectManager = new ProjectManager();
		if (!testInit) documentationManager = new DocumentationManager();
		formatManager = new FormatManager();
		fileManager = new FileManager();
		updateManager = new UpdateManager();

		// Initialize it first, otherwise we can't get the settings
		pathManager.initialize(null);
		
		settings = new Properties();
		File settingsFile = pathManager.getProgramFile("config.properties");
		if (settingsFile.exists()) {
			try (InputStream stream = new FileInputStream(settingsFile)) {
				settings.load(stream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		pathManager.loadSettings(settings);
		
		// The path managers might be used in other manager initialization methods,
		// so we ensure we initialize them first.
		messageManager.initialize(settings);
		gameManager.initialize(settings);
		fileManager.initialize(settings);
		
		hashManager.initialize(settings);
		projectManager.initialize(settings);
		if (!testInit) documentationManager.initialize(settings);
		formatManager.initialize(settings);

		messageManager.postMessage(MessageType.OnSettingsLoad, settings);
	}
	
	public void saveSettings() {
		messageManager.saveSettings(settings);
		gameManager.saveSettings(settings);
		pathManager.saveSettings(settings);
		hashManager.saveSettings(settings);
		projectManager.saveSettings(settings);
		documentationManager.saveSettings(settings);
		formatManager.saveSettings(settings);
		fileManager.saveSettings(settings);

		messageManager.postMessage(MessageType.OnSettingsSave, settings);
		
		try (OutputStream stream = new FileOutputStream(pathManager.getProgramFile("config.properties"))) {
			settings.store(stream, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void init() {
		init(false);
	}
	
	@Override
	public void stop() {
		documentationManager.dispose();
		formatManager.dispose();
		projectManager.dispose();
		hashManager.dispose();

		fileManager.dispose();
		pathManager.dispose();
		gameManager.dispose();
	}

	@Override
	public void start(Stage primaryStage) {
		
		if (!updateManager.checkUpdate()) {
			Platform.exit();
			return;
		}

	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
