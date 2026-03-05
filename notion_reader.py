
import os
import sys
from notion_client import Client

def get_notion_client():
    notion_api_key = os.getenv("NOTION_API_KEY")
    if not notion_api_key:
        print("Error: NOTION_API_KEY environment variable not set.")
        sys.exit(1)
    return Client(auth=notion_api_key)

def get_page_content_and_subpages(page_id, title, level=0):
    notion = get_notion_client()
    indent = "  " * level
    print(f"\n{indent}=== Lendo: {title} ({page_id}) ===")
    
    try:
        # Lê os blocos da página atual
        blocks = notion.blocks.children.list(block_id=page_id).get("results")
        subpages = []
        
        for block in blocks:
            block_type = block.get("type")
            
            # Extrair texto de blocos comuns
            if block_type in ["paragraph", "heading_1", "heading_2", "heading_3", "bulleted_list_item", "numbered_list_item", "callout", "quote"]:
                rich_text = block[block_type].get("rich_text", [])
                text = "".join([t.get("plain_text", "") for t in rich_text])
                if text:
                    prefix = ""
                    if block_type == "heading_1": prefix = "# "
                    elif block_type == "heading_2": prefix = "## "
                    elif block_type == "heading_3": prefix = "### "
                    elif block_type == "bulleted_list_item": prefix = "* "
                    elif block_type == "numbered_list_item": prefix = "1. "
                    elif block_type == "quote": prefix = "> "
                    print(f"{indent}{prefix}{text}")
            
            # Identificar subpáginas
            elif block_type == "child_page":
                subpages.append((block["id"], block["child_page"]["title"]))
        
        # Mergulhar nas subpáginas
        for sub_id, sub_title in subpages:
            get_page_content_and_subpages(sub_id, sub_title, level + 1)

    except Exception as e:
        print(f"{indent}Erro ao ler bloco: {e}")

def start_recursive_read(main_title):
    notion = get_notion_client()
    try:
        results = notion.search(query=main_title, filter={"property": "object", "value": "page"}).get("results")
        if not results:
            print(f"Página '{main_title}' não encontrada.")
            return

        # Filtrar para garantir que pegamos a página exata (o search é por aproximação)
        main_page = None
        for res in results:
            # Título pode estar em propriedades diferentes dependendo se é página ou base de dados, 
            # mas o search com filter 'page' traz no formato padrão
            res_title = "".join([t["plain_text"] for t in res["properties"]["title"]["title"]])
            if res_title == main_title:
                main_page = res
                break
        
        if not main_page:
            main_page = results[0] # Fallback para a primeira

        get_page_content_and_subpages(main_page["id"], main_title)

    except Exception as e:
        print(f"Erro na busca inicial: {e}")

if __name__ == "__main__":
    title = "Arquitetura Financeira - Bot Alfred"
    if len(sys.argv) > 1:
        title = sys.argv[1]
    start_recursive_read(title)
