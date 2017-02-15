package org.renci.canvas.binning.diagnostic.uncseq.commons;

import org.renci.canvas.binning.core.BinningException;
import org.renci.canvas.binning.core.grch37.diagnostic.AbstractAnnotateVariantsCallable;
import org.renci.canvas.dao.CANVASDAOBeanService;
import org.renci.canvas.dao.CANVASDAOException;
import org.renci.canvas.dao.clinbin.model.DiagnosticBinningJob;
import org.renci.canvas.dao.jpa.CANVASDAOManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotateVariantsCallable extends AbstractAnnotateVariantsCallable {

    private static final Logger logger = LoggerFactory.getLogger(AnnotateVariantsCallable.class);

    public AnnotateVariantsCallable(CANVASDAOBeanService daoBean, DiagnosticBinningJob binningJob) {
        super(daoBean, binningJob);
    }

    // exists for testing
    public static void main(String[] args) {
        try {
            CANVASDAOManager daoMgr = CANVASDAOManager.getInstance();
            DiagnosticBinningJob binningJob = daoMgr.getDAOBean().getDiagnosticBinningJobDAO().findById(4218);
            AnnotateVariantsCallable callable = new AnnotateVariantsCallable(daoMgr.getDAOBean(), binningJob);
            callable.call();
        } catch (CANVASDAOException | BinningException e) {
            e.printStackTrace();
        }
    }

}
