package org.gigahub.radio.dao.generator;

import java.io.File;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;

/**
 * Created by asavinova on 12/10/14.
 */
public class DaoRadioGenerator {

	public static void main(String[] args) throws Exception {

		Schema schema = new Schema(1, "org.gigahub.radio.dao");

		Entity station = schema.addEntity("Station");
		station.addIdProperty();
		station.addStringProperty("uuid").notNull().unique();
		station.addStringProperty("name");

		Entity stream = schema.addEntity("Stream");
		stream.addIdProperty();
		stream.addStringProperty("url");

		Property stationId = stream.addLongProperty("stationId").notNull().getProperty();
		ToMany stationToStreams = station.addToMany(stream, stationId);
		stationToStreams.setName("streams");

		String outDir = "app/src/dao/java";

		new File(outDir).mkdirs();

		new DaoGenerator().generateAll(schema, outDir);
	}
}
