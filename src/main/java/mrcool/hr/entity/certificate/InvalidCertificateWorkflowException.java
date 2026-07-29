package mrcool.hr.entity.certificate;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidCertificateWorkflowException extends RuntimeException {
    public InvalidCertificateWorkflowException(String message) {
        super(message);
    }
}
