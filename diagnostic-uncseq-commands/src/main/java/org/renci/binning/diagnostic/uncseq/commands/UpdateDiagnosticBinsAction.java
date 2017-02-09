package org.renci.binning.diagnostic.uncseq.commands;

import java.util.Date;
import java.util.concurrent.Executors;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.renci.binning.diagnostic.uncseq.commons.UpdateDiagnosticBinsCallable;
import org.renci.canvas.dao.CANVASDAOBeanService;
import org.renci.canvas.dao.CANVASDAOException;
import org.renci.canvas.dao.clinbin.model.DiagnosticBinningJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "diagnostic-uncseq", name = "update-diagnostic-bins", description = "Update Diagnostic Bins")
@Service
public class UpdateDiagnosticBinsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(UpdateDiagnosticBinsAction.class);

    @Reference
    private CANVASDAOBeanService daoBeanService;

    @Option(name = "--binningJobId", description = "DiagnosticBinningJob Identifier", required = true, multiValued = false)
    private Integer binningJobId;

    public UpdateDiagnosticBinsAction() {
        super();
    }

    @Override
    public Object execute() throws Exception {
        logger.debug("ENTERING execute()");

        DiagnosticBinningJob binningJob = daoBeanService.getDiagnosticBinningJobDAO().findById(binningJobId);
        logger.info(binningJob.toString());

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                binningJob.setStatus(daoBeanService.getDiagnosticStatusTypeDAO().findById("Updating dx bins"));
                daoBeanService.getDiagnosticBinningJobDAO().save(binningJob);

                Executors.newSingleThreadExecutor().submit(new UpdateDiagnosticBinsCallable(daoBeanService, binningJob)).get();

                binningJob.setStatus(daoBeanService.getDiagnosticStatusTypeDAO().findById("Updated dx bins"));
                daoBeanService.getDiagnosticBinningJobDAO().save(binningJob);

            } catch (Exception e) {
                try {
                    binningJob.setStop(new Date());
                    binningJob.setFailureMessage(e.getMessage());
                    binningJob.setStatus(daoBeanService.getDiagnosticStatusTypeDAO().findById("Failed"));
                    daoBeanService.getDiagnosticBinningJobDAO().save(binningJob);
                } catch (CANVASDAOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        return null;
    }

}
