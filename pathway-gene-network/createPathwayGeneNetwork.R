#' Create a Pathway-Gene Bipartite Network in Cytoscape
#' Author: mkutmon
#'
#' @param enrich_df     Data frame of significant clusterProfiler enrichment results.
#'                      Expected columns: ID, Description, geneID, p.adjust
#' @param title         Cytoscape network title (default: "Pathway-Gene Network").
#' @param collection    Cytoscape collection name (default: "Enrichment Analysis").
#' @param style_name    Visual style name (default: "PathwayGene").
#' @param pathway_color Hex color for pathway nodes (default: "#4E79A7").
#' @param gene_color    Hex color for gene nodes (default: "#F28E2B").
#' @param layout        Cytoscape layout algorithm (default: "force-directed").
#'
#' @return Invisibly returns a list with the node and edge tables used.

createPathwayGeneNetwork <- function(
    enrich_df,
    title         = "Pathway-Gene Network",
    collection    = "Enrichment Analysis",
    style_name    = "PathwayGene",
    pathway_color = "#4E79A7",
    gene_color    = "#F28E2B",
    layout        = "force-directed"
) {
  
  if (nrow(enrich_df) == 0) stop("enrich_df is empty.")
  message("Building network for ", nrow(enrich_df), " pathways.")

  # --- Build edge list --------------------------------------------------
  edges <- enrich_df %>%
    dplyr::select(ID, geneID) %>%
    dplyr::mutate(genes = strsplit(geneID, "/")) %>%
    tidyr::unnest(genes) %>%
    dplyr::select(source = ID, target = genes)

  # --- Build node tables ------------------------------------------------
  pathway_nodes <- enrich_df %>%
    dplyr::transmute(
      id         = ID,
      label      = Description,
      type       = "pathway",
      p.adjust   = p.adjust,
      gene_count = lengths(strsplit(geneID, "/"))
    )

  gene_nodes <- data.frame(
    id    = unique(edges$target),
    label = unique(edges$target),
    type  = "gene",
    stringsAsFactors = FALSE
  )

  nodes <- dplyr::bind_rows(pathway_nodes, gene_nodes)

  # --- Connect to Cytoscape & create network ----------------------------
  RCy3::cytoscapePing()

  RCy3::createNetworkFromDataFrames(
    nodes      = nodes,
    edges      = edges,
    title      = title,
    collection = collection
  )

  # --- Visual style -----------------------------------------------------
  existing_styles <- RCy3::getVisualStyleNames()
  if (style_name %in% existing_styles) RCy3::deleteVisualStyle(style_name)

  RCy3::createVisualStyle(
    style_name,
    defaults = list(NODE_SHAPE = "ellipse", NODE_SIZE = 50)
  )

  RCy3::setNodeShapeMapping(
    table.column        = "type",
    table.column.values = c("pathway", "gene"),
    shapes              = c("ROUND_RECTANGLE", "ELLIPSE"),
    style.name          = style_name
  )

  RCy3::setNodeColorMapping(
    table.column        = "type",
    table.column.values = c("pathway", "gene"),
    colors              = c(pathway_color, gene_color),
    mapping.type        = "d",
    style.name          = style_name
  )

  RCy3::setNodeSizeMapping(
    table.column        = "gene_count",
    table.column.values = range(pathway_nodes$gene_count),
    sizes               = c(40, 100),
    mapping.type        = "c",
    style.name          = style_name,
    default.size        = 30
  )

  RCy3::setNodeLabelMapping("label", style.name = style_name)
  RCy3::setVisualStyle(style_name)

  # --- Layout -----------------------------------------------------------
  RCy3::layoutNetwork(layout)
}
