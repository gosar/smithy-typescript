package software.amazon.smithy.typescript.codegen;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;
import software.amazon.smithy.build.MockManifest;
import software.amazon.smithy.codegen.core.Symbol;
import software.amazon.smithy.codegen.core.SymbolProvider;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.utils.ListUtils;

public class TypeScriptDelegatorTest {
    @Test
    public void vendsWritersForShapes() {
        Model model = createModel();
        Shape fooShape = model.expectShape(ShapeId.from("smithy.example#Foo"));
        SymbolProvider provider = createProvider();
        MockManifest manifest = new MockManifest();
        TypeScriptSettings settings = new TypeScriptSettings();
        TypeScriptDelegator delegator = new TypeScriptDelegator(
                settings, model, manifest, provider, ListUtils.of());

        delegator.useShapeWriter(fooShape, writer -> writer.write("Hello!"));
        delegator.flushWriters();

        assertThat(manifest.getFileString("Foo.txt").get(), equalTo("Hello!\n"));
    }

    @Test
    public void addsBuiltinDependencies() {
        Model model = createModel();
        SymbolProvider provider = createProvider();
        MockManifest manifest = new MockManifest();
        TypeScriptSettings settings = new TypeScriptSettings();
        TypeScriptDelegator delegator = new TypeScriptDelegator(
                settings, model, manifest, provider, ListUtils.of());

        assertThat(delegator.getDependencies(), equalTo(TypeScriptDependency.getUnconditionalDependencies()));
    }

    @Test
    public void appendsToOpenedWriterWithNewline() {
        Model model = createModel();
        Shape fooShape = model.expectShape(ShapeId.from("smithy.example#Foo"));
        SymbolProvider provider = createProvider();
        MockManifest manifest = new MockManifest();
        TypeScriptSettings settings = new TypeScriptSettings();
        TypeScriptDelegator delegator = new TypeScriptDelegator(
                settings, model, manifest, provider, ListUtils.of());

        delegator.useShapeWriter(fooShape, writer -> writer.write("Hello!"));
        delegator.useShapeWriter(fooShape, writer -> writer.write("Goodbye!"));
        delegator.flushWriters();

        assertThat(manifest.getFileString("Foo.txt").get(), equalTo("Hello!\n\nGoodbye!\n"));
    }

    private static Model createModel() {
        return Model.assembler()
                .addImport(TypeScriptDelegatorTest.class.getResource("testmodel.smithy"))
                .assemble()
                .unwrap();
    }

    private static SymbolProvider createProvider() {
        return shape -> Symbol.builder()
                .name(shape.getId().getName())
                .namespace(shape.getId().getNamespace(), "/")
                .definitionFile(shape.getId().getName() + ".txt")
                .build();
    }
}
