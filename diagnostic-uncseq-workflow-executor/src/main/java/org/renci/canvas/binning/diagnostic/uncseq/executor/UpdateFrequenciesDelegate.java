package org.renci.canvas.binning.diagnostic.uncseq.executor;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.commons.collections4.CollectionUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.renci.canvas.binning.diagnostic.uncseq.commons.UpdateFrequenciesCallable;
import org.renci.canvas.dao.CANVASDAOBeanService;
import org.renci.canvas.dao.CANVASDAOException;
import org.renci.canvas.dao.clinbin.model.DiagnosticBinningJob;
import org.renci.canvas.dao.clinbin.model.MaxFrequency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateFrequenciesDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(UpdateFrequenciesDelegate.class);

    public UpdateFrequenciesDelegate() {
        super();
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.debug("ENTERING execute(DelegateExecution)");

        Map<String, Object> variables = execution.getVariables();

        BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
        ServiceReference<CANVASDAOBeanService> daoBeanServiceReference = bundleContext.getServiceReference(CANVASDAOBeanService.class);
        CANVASDAOBeanService daoBean = bundleContext.getService(daoBeanServiceReference);

        Integer binningJobId = null;
        Object o = variables.get("binningJobId");
        if (o != null && o instanceof Integer) {
            binningJobId = (Integer) o;
        }

        try {
            DiagnosticBinningJob binningJob = daoBean.getDiagnosticBinningJobDAO().findById(binningJobId);
            binningJob.setStatus(daoBean.getDiagnosticStatusTypeDAO().findById("Updating frequency table"));
            daoBean.getDiagnosticBinningJobDAO().save(binningJob);
            logger.info(binningJob.toString());

            List<MaxFrequency> results = Executors.newSingleThreadExecutor().submit(new UpdateFrequenciesCallable(daoBean, binningJob))
                    .get();

            if (CollectionUtils.isNotEmpty(results)) {
                logger.info(String.format("saving %d new MaxFrequency instances", results.size()));
                for (MaxFrequency maxFrequency : results) {
                    logger.info(maxFrequency.toString());
                    daoBean.getMaxFrequencyDAO().save(maxFrequency);
                }
            }

            binningJob = daoBean.getDiagnosticBinningJobDAO().findById(binningJobId);
            binningJob.setStatus(daoBean.getDiagnosticStatusTypeDAO().findById("Updated frequency table"));
            daoBean.getDiagnosticBinningJobDAO().save(binningJob);
            logger.info(binningJob.toString());

        } catch (Exception e) {
            try {
                DiagnosticBinningJob binningJob = daoBean.getDiagnosticBinningJobDAO().findById(binningJobId);
                binningJob.setStop(new Date());
                binningJob.setFailureMessage(e.getMessage());
                binningJob.setStatus(daoBean.getDiagnosticStatusTypeDAO().findById("Failed"));
                daoBean.getDiagnosticBinningJobDAO().save(binningJob);
                logger.info(binningJob.toString());
            } catch (CANVASDAOException e1) {
                e1.printStackTrace();
            }
        }

    }

}
