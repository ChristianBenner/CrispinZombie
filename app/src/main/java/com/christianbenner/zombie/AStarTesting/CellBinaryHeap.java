package com.christianbenner.zombie.AStarTesting;

class Node {
    LegacyCell value;
    Node left;
    Node right;

    Node(LegacyCell value) {
        this.value = value;
        this.left = null;
        this.right = null;
    }
}

public class CellBinaryHeap {
    private Node root;

    // Stupidly inefficient
    public void clear()
    {
        System.out.println("Clearing");

        // Clear all the smallest elements
        Node current = root;
        while (current != null)
        {
            current = removeRecursive(root, min());
        }

        // Clear the root
        root = null;
    }

    public boolean isEmpty()
    {
        return root == null;
    }
    public void remove(LegacyCell value)
    {
        removeRecursive(root, value);

        if(contains(value))
        {
            System.out.println("FAILED TO REMOVE VALUE");
        }
    }

    public void add(LegacyCell value)
    {
        root = addRecursive(root, value);
    }

    public boolean contains(LegacyCell value)
    {
        return containsNodeRecursive(root, value);
    }

    // Fetch the cell with the lowest fCost
    public LegacyCell min()
    {
        return minimumRecursive(root);
    }

    private Node removeRecursive(Node current, LegacyCell value)
    {
        if(current == null)
        {
            System.out.println("Current is null");
            return null;
        }

        if(value == current.value)
        {
            if(current.left == null && current.right == null)
            {
                System.out.println("Both branches are null");
                return null;
            }

            if(current.right == null)
            {
                System.out.println("Right branch is null");
                return current.left;
            }

            if(current.left == null)
            {
                System.out.println("Left branch is null");
                return current.right;
            }

            System.out.println("Both branches contain data");
            LegacyCell smallestValue = minimumRecursive(current.right);
            current.value = smallestValue;
            current.right = removeRecursive(current.right, smallestValue);
            return current;
        }

        if(value.fCost < current.value.fCost)
        {
            System.out.println("Left Recursive");
            current.left = removeRecursive(current.left, value);
            return current;
        }

        System.out.println("Right Recursive");
        current.right = removeRecursive(current.right, value);
        System.out.println("Returning: " + current.value.fCost);
        return current;
        /*
        System.out.println("Removing Node: " + value.fCost);
        if(current != null)
        {
            System.out.println("Current: " + current.value.fCost);
        }
        else
        {
            System.out.println("Current: NULL");
        }
        System.out.println("Root: " + root.value.fCost);
        if(current == null)
        {
            return null;
        }

        if(value == current.value)
        {
            // Code to delete goes here
            if(current.left == null && current.right == null)
            {
                // The value requested is the root and there is no other nodes to shift
                if(current == root)
                {
                    root = null;
                }
                return null;
            }

            if(current.right == null)
            {
                return current.left;
            }

            if(current.right == null)
            {
                return current.right;
            }

            Cell smallestValue = minimumRecursive(current.right);
            current.value = smallestValue;
            current.right = removeRecursive(current.right, smallestValue);
            return current;
        }

        if(value.fCost < current.value.fCost)
        {
            current.left = removeRecursive(current.left, value);
            return current;
        }

        current.right = removeRecursive(current.right, value);
        return current;*/
    }

    private LegacyCell minimumRecursive(Node current)
    {
        return current.left == null ? current.value : minimumRecursive(current.left);
    }

    private boolean containsNodeRecursive(Node current, LegacyCell value)
    {
        if(current == null)
        {
            return false;
        }

        if(value == current.value)
        {
            return true;
        }

        return value.fCost < current.value.fCost
                ? containsNodeRecursive(current.left, value)
                : containsNodeRecursive(current.left, value);
    }

    private Node addRecursive(Node current, LegacyCell value)
    {
        if(current == null)
        {
            return new Node(value);
        }

        if(value.fCost < current.value.fCost)
        {
            current.left = addRecursive(current.left, value);
        }
        else if(value.fCost > current.value.fCost)
        {
            current.right = addRecursive(current.right, value);
        }
        else
        {
            return current;
        }

        return current;
    }
}
