package io.github.indesil.muscarita;

import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.lang.model.util.ElementFilter.constructorsIn;

@SupportedAnnotationTypes({
        "io.github.indesil.muscarita.annotations.BeanConfiguration",
        "io.github.indesil.muscarita.annotations.Component",
        "io.github.indesil.muscarita.annotations.Repository",
        "io.github.indesil.muscarita.annotations.Service",
        "io.github.indesil.muscarita.annotations.Controller"
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class BeanConfigurationProcessor extends AbstractProcessor {
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        List<ExecutableElement> allBeansConstructors = extractAllConstructors(annotations, roundEnv);
        createHelloWorldClass();
        return false;
    }

    private List<ExecutableElement> extractAllConstructors(
            Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        return annotations.stream()
                .map(roundEnv::getElementsAnnotatedWith)
                .flatMap(Collection::stream)
                .filter(MoreElements::isType)
                .map(MoreElements::asType)
                .map(this::extractTypeConstructors)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

    }

    public List<ExecutableElement> extractTypeConstructors(TypeElement typeElement) {
        List<? extends Element> allMembers = processingEnv.getElementUtils().getAllMembers(typeElement);
        return constructorsIn(allMembers);
    }

    private void createHelloWorldClass() {
        MethodSpec main = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                .build();

        TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(main)
                .build();

        JavaFile javaFile = JavaFile.builder("com.example.helloworld", helloWorld)
                .build();

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
