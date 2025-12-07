package dn.jasm.configuration;


import dn.jasm.exception.ApplicationStartRunningException;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationGlobalConfig extends AbstractFailureAnalyzer<ApplicationStartRunningException> {
    @Override
    protected FailureAnalysis analyze(Throwable rootFailure,
                                      ApplicationStartRunningException cause) {
        return new FailureAnalysis("Application was down on start-time cause",
                "Check your start-configuration",cause);
    }
}
