export function screen(items) {
    return {
               desktop: {
                   breakpoint: { max: 3000, min: 1024 },
                   items
               }
           }
}