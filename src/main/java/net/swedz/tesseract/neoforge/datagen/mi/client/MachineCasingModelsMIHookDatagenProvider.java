package net.swedz.tesseract.neoforge.datagen.mi.client;
import aztech.modern_industrialization.client.machines.models.CasingModel;
import aztech.modern_industrialization.client.machines.models.CasingModels;
import aztech.modern_industrialization.client.machines.models.UseBlockModelBakedModel;
import aztech.modern_industrialization.machines.models.MachineCasing;
import net.minecraft.data.*;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.swedz.tesseract.neoforge.compat.mi.hook.MIHookTracker;
import com.google.gson.JsonObject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
/** Writes the new casing format alongside vanilla cube model JSON. */
public final class MachineCasingModelsMIHookDatagenProvider implements DataProvider {
 private final PackOutput output;
 private final String modId;
 private final Map<Identifier,CasingModel.Unbaked> casings = new HashMap<>();
 private final Map<Identifier,JsonObject> models = new HashMap<>();
 public MachineCasingModelsMIHookDatagenProvider(GatherDataEvent event, String modId) { this.output=event.getGenerator().getPackOutput();this.modId=modId; }
 public void imitateBlock(MachineCasing casing, Block block) { casings.put(casing.key,new CasingModel.Unbaked(new UseBlockModelBakedModel.Unbaked(block))); }
 public void cubeBottomTop(MachineCasing casing, Identifier side, Identifier bottom, Identifier top) { cube(casing,"cube_bottom_top", Map.of("side",side,"bottom",bottom,"top",top)); }
 public void cubeAll(MachineCasing casing, Identifier side) { cube(casing,"cube_all", Map.of("all",side)); }
 private void cube(MachineCasing casing,String template,Map<String,Identifier> textures) {
  var modelId=casing.key.withPrefix("machine_casing/");
  var json=new JsonObject();json.addProperty("parent","minecraft:block/"+template);
  var tex=new JsonObject();textures.forEach((k,v)->tex.addProperty(k,v.toString()));json.add("textures",tex);models.put(modelId,json);
  casings.put(casing.key,new CasingModel.Unbaked(new net.minecraft.client.renderer.block.dispatch.SingleVariant.Unbaked(net.minecraft.client.data.models.BlockModelGenerators.plainModel(modelId))));
 }
 @Override public CompletableFuture<?> run(CachedOutput cache) {
  MIHookTracker.getMachineCasingModels(modId).forEach(action->action.accept(this));
  var futures=new ArrayList<CompletableFuture<?>>();
  for (var entry : MIHookTracker.getMachineModels(modId).entrySet()) {
   var props=entry.getValue();
   var overlays=new java.util.HashMap<aztech.modern_industrialization.client.machines.models.OverlayName,Identifier>();
   var json=new JsonObject();props.addToMachineJson(json);
   json.getAsJsonObject("default_overlays").entrySet().forEach(e->overlays.put(aztech.modern_industrialization.client.machines.models.OverlayName.CODEC.byName(e.getKey()), Identifier.parse(e.getValue().getAsString())));
   var model=new aztech.modern_industrialization.client.machines.models.MachineUnbakedModel(props.defaultCasing(),overlays,Map.of(),false);
   var block=BuiltInRegistries.BLOCK.getValue(entry.getKey());
   var definition=net.minecraft.client.data.models.blockstates.MultiVariantGenerator.dispatch(block,net.minecraft.client.data.models.MultiVariant.of(new net.neoforged.neoforge.client.model.generators.blockstate.CustomBlockStateModelBuilder.Simple(model))).create();
   futures.add(DataProvider.saveStable(cache,net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher.CODEC,definition,output.createPathProvider(PackOutput.Target.RESOURCE_PACK,"blockstates").json(entry.getKey())));
   var item=new net.minecraft.client.renderer.item.ClientItem(new aztech.modern_industrialization.client.machines.models.MachineItemModel.Unbaked(block),net.minecraft.client.renderer.item.ClientItem.Properties.DEFAULT);
   futures.add(DataProvider.saveStable(cache,net.minecraft.client.renderer.item.ClientItem.CODEC,item,output.createPathProvider(PackOutput.Target.RESOURCE_PACK,"items").json(entry.getKey())));
  }
  futures.add(DataProvider.saveAll(cache,CasingModel.Unbaked.CODEC,output.createPathProvider(PackOutput.Target.RESOURCE_PACK,CasingModels.FOLDER_NAME),casings));
  var paths=output.createPathProvider(PackOutput.Target.RESOURCE_PACK,"models");
  models.forEach((id,json)->futures.add(DataProvider.saveStable(cache,json,paths.json(id))));
  return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
 }
 @Override public String getName() { return "Tesseract MI casings: "+modId; }
}
