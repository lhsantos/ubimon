using System.Collections.Generic;
using UnityEngine;

public struct Insets
{
    public int top;
    public int bottom;
    public int left;
    public int right;

    public Insets(int top, int bottom, int left, int right)
    {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
    }
}

public class UbimonIcon
{
    public Ubimon ubimon;
    public Texture2D image;
    public UbimonIconCanvas canvas;
    public Vector2 screenSize;
    public Insets insets;
}

public class UbimonIconCanvas
{
    public int x, y, width, height;

    public int area { get { return width * height; } }

    public UbimonIconCanvas()
        : this(0, 0, 0, 0) { }

    public UbimonIconCanvas(int x, int y, int width, int height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public bool Fits(int width, int height)
    {
        return (width <= this.width) && (height <= this.height);
    }
}

public class UbimonContainer
{
    public static readonly Vector2 refResolution = new Vector2(341.5f, 192);
    private List<UbimonIcon> _icons;
    public List<UbimonIcon> icons
    {
        get { return _icons; }
        set
        {
            _icons = value;
            selected = null;
        }
    }
    public Ubimon selected { get; private set; }

    private float prop { get { return view.height / refResolution.y; } }
    private UbimonIconCanvas view;

    public UbimonContainer(int x, int y, int width, int height)
    {
        view = new UbimonIconCanvas(x, y, width, height);
    }

    public void OnGUI()
    {
        if (icons != null)
        {
            foreach (var icon in icons)
            {
                GUI.enabled = (icon.ubimon != selected);
                Rect r = new Rect(
                    icon.canvas.x + icon.insets.left, icon.canvas.y + icon.insets.top, icon.screenSize.x, icon.screenSize.y);
                if (GUI.Button(r, icon.image))
                    selected = icon.ubimon;
                GUI.enabled = true;
            }
        }
    }

    public List<UbimonIcon> Fit(List<Ubimon> list, Ubimon toAdd)
    {
        List<UbimonIcon> result = SortedIcons(list, toAdd);

        var heap = new Heap();
        heap.Push(view);
        for (int i = 0; i < result.Count; ++i)
        {
            UbimonIconCanvas canvas = result[i].canvas;

            // Gets the largest available region...
            UbimonIconCanvas r = heap.Pop();
            if (!r.Fits(canvas.width, canvas.height))
                return null;

            // Uses this region's position...
            canvas.x = r.x;
            canvas.y = r.y;

            // Splits the largest region in remaining spaces...
            heap.Push(new UbimonIconCanvas(r.x + canvas.width, r.y, r.width - canvas.width, r.height));
            heap.Push(new UbimonIconCanvas(r.x, r.y + canvas.height, canvas.width, r.height - canvas.height));
        }

        return result;
    }

    private List<UbimonIcon> SortedIcons(List<Ubimon> list, Ubimon toAdd)
    {
        List<UbimonIcon> result = new List<UbimonIcon>(list.Count + 1);
        foreach (var u in list)
            result.Add(CreateIcon(u));
        if (toAdd != null)
            result.Add(CreateIcon(toAdd));

        result.Sort((a, b) => b.canvas.area.CompareTo(a.canvas.area));
        return result;
    }

    private UbimonIcon CreateIcon(Ubimon ubimon)
    {
        UbimonIcon ui = new UbimonIcon();

        ui.ubimon = ubimon;
        ui.image = ubimon.prototype.texture;
        ui.screenSize = prop * ubimon.prototype.textureSize;
        ui.insets = new Insets(3, 3, 3, 3);
        ui.canvas = new UbimonIconCanvas(
                0, 0,
                Mathf.CeilToInt(ui.screenSize.x + ui.insets.left + ui.insets.right),
                Mathf.CeilToInt(ui.screenSize.y + ui.insets.top + ui.insets.bottom));

        return ui;
    }

    private class Heap
    {
        private List<UbimonIconCanvas> heap = new List<UbimonIconCanvas>();

        public void Push(UbimonIconCanvas canvas)
        {
            int i = heap.Count, parent;
            heap.Add(canvas);

            while ((i > 0) && (heap[parent = ((i - 1) >> 1)].area < heap[i].area))
            {
                var aux = heap[parent];
                heap[parent] = heap[i];
                heap[i] = aux;
                i = parent;
            }
        }

        public UbimonIconCanvas Pop()
        {
            if (heap.Count > 0)
            {
                UbimonIconCanvas c = heap[0];
                heap[0] = heap[heap.Count - 1];
                heap.RemoveAt(heap.Count - 1);
                Heapify(0);
                return c;
            }
            return null;
        }

        private void Heapify(int i)
        {
            int left = (i << 1) | 1;
            int right = left + 1;

            int max = i;
            if ((left < heap.Count) && (heap[left].area > heap[i].area))
                max = left;
            if ((right < heap.Count) && (heap[right].area > heap[max].area))
                max = right;

            if (max != i)
            {
                var aux = heap[i];
                heap[i] = heap[max];
                heap[max] = aux;

                Heapify(max);
            }
        }
    }
}
