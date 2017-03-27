package fr.inria.spirals.repairnator.serializer.csv;

import java.io.BufferedWriter;
import java.util.Date;

import fr.inria.spirals.repairnator.process.inspectors.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.spirals.jtravis.entities.Build;
import fr.inria.spirals.repairnator.process.inspectors.ProjectInspector;
import fr.inria.spirals.repairnator.serializer.AbstractDataSerializer;
import fr.inria.spirals.repairnator.Utils;

public class CSVSerializer4RepairNator extends AbstractDataSerializer {
    private static final String FIRST_LINE = "BuildId" + CSVSerializerUtils.SEPARATOR + "Slug"
            + CSVSerializerUtils.SEPARATOR + "Status" + CSVSerializerUtils.SEPARATOR + "PRNumber"
            + CSVSerializerUtils.SEPARATOR + "Build Timestamp" + CSVSerializerUtils.SEPARATOR + "Build Date"
            + CSVSerializerUtils.SEPARATOR + "Real Status" + CSVSerializerUtils.SEPARATOR + "Collection Timestamp";

    private final Logger logger = LoggerFactory.getLogger(CSVSerializer4RepairNator.class);

    private static BufferedWriter stream;

    public CSVSerializer4RepairNator(String outputPath) {
        super();
        stream = CSVSerializerUtils.openFile(outputPath, FIRST_LINE);
    }

    private void writeData(int buildid, String slug, String state, String realState, int prNumber, Date date) {
        String buildId = buildid + "";
        String prNumberStr = prNumber + "";
        String line = buildId + CSVSerializerUtils.SEPARATOR + slug + CSVSerializerUtils.SEPARATOR + state
                + CSVSerializerUtils.SEPARATOR + prNumberStr + CSVSerializerUtils.SEPARATOR
                + Utils.formatCompleteDate(date) + CSVSerializerUtils.SEPARATOR
                + Utils.formatOnlyDay(date) + CSVSerializerUtils.SEPARATOR + realState
                + CSVSerializerUtils.SEPARATOR + Utils.formatCompleteDate(new Date());
        CSVSerializerUtils.writeNewLine(stream, line);
    }

    @Override
    public void serializeData(ProjectInspector inspector) {
        Build build = inspector.getBuild();
        JobStatus jobStatus = inspector.getJobStatus();

        String state = this.getPrettyPrintState(jobStatus);

        String realState = (jobStatus.getState() != null) ? jobStatus.getState().name() : "null";

        this.writeData(build.getId(), build.getRepository().getSlug(), state, realState, build.getPullRequestNumber(),
                build.getFinishedAt());
    }
}
