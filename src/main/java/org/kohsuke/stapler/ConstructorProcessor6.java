package org.kohsuke.stapler;

import org.kohsuke.MetaInfServices;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementScanner6;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings({"Since15"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("*")
@MetaInfServices(Processor.class)
public class ConstructorProcessor6 extends AbstractProcessorImpl {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        ElementScanner6<Void, Void> scanner = new ElementScanner6<Void, Void>() {
            @Override
            public Void visitExecutable(ExecutableElement e, Void aVoid) {
                if(e.getAnnotation(DataBoundConstructor.class)!=null) {
                    write(e);
                } else {
                    String javadoc = getJavadoc(e);
                    if(javadoc!=null && javadoc.contains("@stapler-constructor")) {
                        write(e);
                    }
                }

                return super.visitExecutable(e, aVoid);
            }
        };

        for( Element e : roundEnv.getRootElements() )
            scanner.scan(e,null);

        return false;
    }

    private void write(ExecutableElement c) {
        try {
            StringBuilder buf = new StringBuilder();
            for( VariableElement p : c.getParameters() ) {
                if(buf.length()>0)  buf.append(',');
                buf.append(p.getSimpleName());
            }

            TypeElement t = (TypeElement) c.getEnclosingElement();
            String name = t.getQualifiedName().toString().replace('.', '/') + ".stapler";
            notice("Generating " + name, c);

            Properties p = new Properties();
            p.put("constructor",buf.toString());
            writePropertyFile(p, name);
        } catch (IOException x) {
            error(x.toString());
        }
    }
}