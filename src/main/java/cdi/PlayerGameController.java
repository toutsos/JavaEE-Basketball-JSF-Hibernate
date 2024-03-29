package cdi;

import entities.PlayerGame;
import cdi.util.JsfUtil;
import cdi.util.JsfUtil.PersistAction;
import ejb.PlayerGameFacade;
import entities.Game;
import entities.Player;
import cdi.PlayerController;

import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

@Named("playerGameController")
@SessionScoped
public class PlayerGameController implements Serializable {
    
    @Inject private PlayerController playerController;
    

    @EJB
    private ejb.PlayerGameFacade ejbFacade;
    private List<PlayerGame> items = null;
    private PlayerGame selected;

    public PlayerGameController() {
    }

    public PlayerGame getSelected() {
        return selected;
    }

    public void setSelected(PlayerGame selected) {
        this.selected = selected;
    }

    private PlayerGameFacade getFacade() {
        return ejbFacade;
    }

    public PlayerGame prepareCreate() {
        selected = new PlayerGame();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/Bundle").getString("PlayerGameCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/Bundle").getString("PlayerGameUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/Bundle").getString("PlayerGameDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void generatePlayers(Game selected) {
        List<Player> players = playerController.getSortPlayerFromTrainingNumber();
        try {
            for (int i = 0; i < players.size(); i++) {
                PlayerGame pg = new PlayerGame(selected, players.get(i));
                getFacade().create(pg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<PlayerGame> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
    }

    
    
    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            try {
                if (persistAction != PersistAction.DELETE) {
                    getFacade().edit(selected);
                } else {
                    getFacade().remove(selected);
                }
                JsfUtil.addSuccessMessage(successMessage);
            } catch (EJBException ex) {
                String msg = "";
                Throwable cause = ex.getCause();
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage(msg);
                } else {
                    JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            }
        }
    }

    public PlayerGame getPlayerGame(java.lang.Integer id) {
        return getFacade().find(id);
    }

    public List<PlayerGame> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<PlayerGame> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }
    
    public List<Player> getPlayerFromGame(Game game){
        return getFacade().getPlayersFromGame(game);
    }

    @FacesConverter(forClass = PlayerGame.class)
    public static class PlayerGameControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            PlayerGameController controller = (PlayerGameController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "playerGameController");
            return controller.getPlayerGame(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof PlayerGame) {
                PlayerGame o = (PlayerGame) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), PlayerGame.class.getName()});
                return null;
            }
        }

    }

}
