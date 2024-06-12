/* ***************************************************************************** *
 * Copyright (c) 2024 OBEO. All rights reserved.
 *
 * Contributors:
 *    Obeo - initial API and implementation
 * ***************************************************************************** */
package org.polarsys.capella.vptools;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.ui.views.properties.tabbed.ISection;

/**
 * The HelperInfo stores helperClass, sections and menu contribution of Capella
 * model classes.
 *
 * @author nperansin
 */
public class HelperInfo {

    private final EClass eClass;
    private final String fullClassname;
    private final Class<?> helperClass;
    private final Class<?> menuContributorBaseClass;
    private final Class<? extends ISection> sectionClass;

    public HelperInfo(EClass eClass, Class<?> helperClass,
            Class<?> menuContributorBaseClass, Class<? extends ISection> sectionClass) {
        this.eClass = eClass;
        this.fullClassname = getHelperFullClassname(helperClass);
        this.helperClass = helperClass;
        this.menuContributorBaseClass = menuContributorBaseClass;
        this.sectionClass = filterSectionClass(sectionClass);
    }


    public Class<?> getHelperClass() {
        return helperClass;
    }

    public EClass getEClass() {
        return eClass;
    }

    public String getFullClassname() {
        return fullClassname;
    }

    public Class<?> getMenuContributorBaseClass() {
        return menuContributorBaseClass;
    }

    public Class<?> getSectionClass() {
        return sectionClass;
    }

    static String getHelperFullClassname(Class<?> helperClass) {
        String fullClassname = null;
        if (helperClass != null) {
            fullClassname = Stream.of(helperClass.getDeclaredMethods())
                .filter(it -> "doSwitch".equals(it.getName()))
                .map(Method::getParameterTypes)
                .filter(it -> it.length == 2
                    && EStructuralFeature.class.equals(it[1]))
                .map(it -> it[0])
                .findFirst()
                .get() // required if helper is provided
                .getCanonicalName();
        }
        return fullClassname;
    }

    static List<EClass> getLinearSuperTypes(EClass source) {
        List<EClass> result = getInheritancePath(source);
        for (int index = 0; index < result.size() - 1; index++) {
            EClass current = result.get(index);
            if (result.subList(index + 1, result.size()).contains(current)) {
                result.remove(index);
                index--;
            }
        }
        return result;
    }

    static List<EClass> getInheritancePath(EClass source) {
        List<EClass> result = new ArrayList<>(source.getESuperTypes().size());
        for (EClass superClass : source.getESuperTypes()) {
            result.add(superClass);
            result.addAll(getInheritancePath(superClass));
        }
        return result;
    }
    
    static Class<? extends ISection> filterSectionClass(Class<? extends ISection> sectionClass) {
        return sectionClass != null && !Modifier.isAbstract(sectionClass.getModifiers())
                ? sectionClass
                : null;
    }
}
