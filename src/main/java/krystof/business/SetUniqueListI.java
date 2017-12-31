package krystof.business;

import org.apache.commons.collections.list.SetUniqueList;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;


@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SetUniqueListI extends SetUniqueList {

    public SetUniqueListI() {

        this(new ArrayList(), new HashSet());
    }

    public SetUniqueListI(List list, Set set) {
        super(list, set);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Label)) return false;

        SetUniqueListI otherList = (SetUniqueListI) o;
        if (this.size() != otherList.size()) {
            return false;
        }
        ArrayList copyThis = new ArrayList(this);
        ArrayList copyother = new ArrayList(otherList);
        Collections.sort(copyThis);
        Collections.sort(copyother);
        return copyThis.containsAll(copyother) && copyother.containsAll(copyThis);
    }

    @Override
    public int hashCode() {
        System.out.println("--------------");
        ArrayList copy = new ArrayList(this);
        Collections.sort(copy);
        int hashCode = 1;
        for (Object o : copy) {
            System.out.println(o.toString());
            System.out.println(o.hashCode());
            hashCode = 31 * hashCode + (o == null ? 0 : o.hashCode());

        }
        System.out.println("compelte hashcode"+hashCode);
        System.out.println("-----------------");
        return hashCode;

    }
}
