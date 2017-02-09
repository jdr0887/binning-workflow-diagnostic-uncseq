package org.renci.binning.diagnostic.uncseq.commons;

import java.util.concurrent.Executors;

import org.renci.binning.core.diagnostic.AbstractGenerateReportCallable;
import org.renci.canvas.dao.CANVASDAOBeanService;
import org.renci.canvas.dao.CANVASDAOException;
import org.renci.canvas.dao.clinbin.model.DiagnosticBinningJob;
import org.renci.canvas.dao.jpa.CANVASDAOManager;

public class GenerateReportCallable extends AbstractGenerateReportCallable {

    public GenerateReportCallable(CANVASDAOBeanService daoBean, DiagnosticBinningJob binningJob) {
        super(daoBean, binningJob);
    }

    public static void main(String[] args) {
        try {
            CANVASDAOManager daoMgr = CANVASDAOManager.getInstance();
            DiagnosticBinningJob binningJob = daoMgr.getDAOBean().getDiagnosticBinningJobDAO().findById(4218);
            GenerateReportCallable runnable = new GenerateReportCallable(daoMgr.getDAOBean(), binningJob);
            Executors.newSingleThreadExecutor().submit(runnable);
        } catch (CANVASDAOException e) {
            e.printStackTrace();
        }
    }
}
