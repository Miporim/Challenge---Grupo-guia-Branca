package br.com.fiap.aguiabranca.shared;

public class EmailJaCadastradoException extends ConflitoException {
    public EmailJaCadastradoException(String email) {
        super("E-mail já cadastrado: " + email);
    }
}
