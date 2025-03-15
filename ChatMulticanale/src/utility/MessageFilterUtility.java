package utility;

import model.Messaggio;

import java.util.ArrayList;
import java.util.List;

public class MessageFilterUtility {
    private final List<Messaggio> originalMessages;
    private List<Messaggio> filteredMessages;

    public MessageFilterUtility(List<Messaggio> messaggiList) {
        this.originalMessages = new ArrayList<>(messaggiList);
        this.filteredMessages = new ArrayList<>(messaggiList);
    }

    /**
     * Filtra i messaggi in base all'username dell'utente.
     *
     * @param username L'username dell'utente per il quale filtrare i messaggi.
     */
    public boolean filterByUser(String username) {
        filteredMessages = new ArrayList<>();

        for (Messaggio messaggio : originalMessages) {
            if (messaggio.getMittente().equals(username)) {
                filteredMessages.add(messaggio);
            }
        }
        if (filteredMessages.size()!=0){return false;}
        else return true;
    }

    /**
     * Rimuove il filtro, rendendo la lista filtrata identica alla lista originale.
     */
    public void removeFilter() {
        this.filteredMessages = new ArrayList<>(originalMessages);
    }

    public List<Messaggio> getFilteredMessages() {
        return filteredMessages;
    }

}