const brands = ["Dior", "Chanel", "Versace", "Gucci", "Armani", "Prada",
                "Hugo Boss", "Tom Ford", "YSL", "Givenchy"];
const genders = ["MEN", "WOMEN", "UNISEX"];
const scents = ["AMBER", "AROMATIC", "GOURMAND", "WOODY", "GREEN", "FLORAL", "AQUATIC",
                "SPICY", "FRESH", "SWEET", "FRUITY", "FOUGERE", "CITRUS", "CHYPRE"];
const volumes = [30, 50, 100, 200];
const pick = a => a[Math.floor(Math.random() * a.length)];

const docs = [];
for (let i = 0; i < 5000; i++) {
  const scentSet = new Set();
  const n = 1 + Math.floor(Math.random() * 3);
  while (scentSet.size < n) scentSet.add(pick(scents));

  const vols = volumes.filter(() => Math.random() < 0.6);
  if (vols.length === 0) vols.push(100);

  docs.push({
    name: "Test perfume " + i,
    brand: pick(brands),
    description: "seed",
    gender: pick(genders),
    scentFamilies: Array.from(scentSet),
    mainImage: "https://cdn.example.com/seed/" + i + ".jpg",
    gallery: [],
    variants: vols.map(v => ({
      sku: "SEED-" + i + "-" + v,
      volumeMl: v,
      price: NumberDecimal((v * (10 + Math.random() * 40)).toFixed(2)),
      active: true
    })),
    active: Math.random() < 0.95,
    createdAt: new Date(Date.now() - Math.floor(Math.random() * 365 * 24 * 3600 * 1000)),
    updatedAt: new Date(),
    version: NumberLong(0)
  });
}
db.products.insertMany(docs);
print("inserted " + docs.length);