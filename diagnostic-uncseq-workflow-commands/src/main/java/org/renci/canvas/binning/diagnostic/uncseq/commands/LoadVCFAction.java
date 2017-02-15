package org.renci.canvas.binning.diagnostic.uncseq.commands;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.renci.canvas.binning.diagnostic.uncseq.commons.LoadVCFCallable;
import org.renci.canvas.dao.CANVASDAOBeanService;
import org.renci.canvas.dao.CANVASDAOException;
import org.renci.canvas.dao.clinbin.model.DiagnosticBinningJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "diagnostic-uncseq", name = "load-vcf", description = "Load VCF")
@Service
public class LoadVCFAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(LoadVCFAction.class);

    @Reference
    private CANVASDAOBeanService daoBeanService;

    @Option(name = "--binningJobId", description = "DiagnosticBinningJob Identifier", required = true, multiValued = false)
    private Integer binningJobId;

    public LoadVCFAction() {
        super();
    }

    @Override
    public Object execute() throws Exception {
        logger.debug("ENTERING execute()");

        DiagnosticBinningJob binningJob = daoBeanService.getDiagnosticBinningJobDAO().findById(binningJobId);
        logger.info(binningJob.toString());

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                binningJob.setStatus(daoBeanService.getDiagnosticStatusTypeDAO().findById("VCF loading"));
                daoBeanService.getDiagnosticBinningJobDAO().save(binningJob);

                ExecutorService es = Executors.newSingleThreadExecutor();
                es.submit(new LoadVCFCallable(daoBeanService, binningJob));
                es.shutdown();
                es.awaitTermination(1L, TimeUnit.DAYS);

                binningJob.setStatus(daoBeanService.getDiagnosticStatusTypeDAO().findById("VCF loaded"));
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
