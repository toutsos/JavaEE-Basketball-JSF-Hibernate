package cdi;

import entities.PlayerTraining;
import cdi.util.JsfUtil;
import cdi.util.JsfUtil.PersistAction;
import ejb.PlayerTrainingFacade;
import entities.Player;
import entities.Training;

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

@Named("playerTrainingController")
@SessionScoped
public class PlayerTrainingController implements Serializable {

    @EJB
    private ejb.PlayerTrainingFacade ejbFacade;
    private List<PlayerTraining> items = null;
    private PlayerTraining selected;

    public PlayerTrainingController() {
    }

    public PlayerTraining getSelected() {
        return selected;
    }

    public void setSelected(PlayerTraining selected) {
        this.selected = selected;
    }

    private PlayerTrainingFacade getFacade() {
        return ejbFacade;
    }

    public PlayerTraining prepareCreate(Training training) {
        selected = new PlayerTraining();
        selected.setTraining(training);
        System.out.println(selected.getTraining());
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/Bundle").getString("PlayerTrainingCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/Bundle").getString("PlayerTrainingUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/Bundle").getString("PlayerTrainingDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<PlayerTraining> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
    }
    
    
    public List<Player> getPlayersTraining(Training training){
        List<Player> players;
        players = getFacade().getPlayerFromTraining(training);
        return players;
    }
    
    public List<Player> getPlayersFromTraining(Training training){
        List<Player> players;
        players = getFacade().getPlayerFromTraining(training);
        return players;
    }
    
    public int getRankForPlayer(Training training,Player player){
      
            int rank = getFacade().getRankForPlayer(training, player);
            return rank;
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

    public PlayerTraining getPlayerTraining(java.lang.Integer id) {
        return getFacade().find(id);
    }

    public List<PlayerTraining> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<PlayerTraining> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }
    

    @FacesConverter(forClass = PlayerTraining.class)
    public static class PlayerTrainingControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            PlayerTrainingController controller = (PlayerTrainingController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "playerTrainingController");
            return controller.getPlayerTraining(getKey(value));
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
            if (object instanceof PlayerTraining) {
                PlayerTraining o = (PlayerTraining) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), PlayerTraining.class.getName()});
                return null;
            }
        }

    }

}
